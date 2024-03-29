package nonamecrackers2.mobbattlemusic.client.manager;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.TieredItem;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;
import nonamecrackers2.mobbattlemusic.client.resource.MusicTracksManager;
import nonamecrackers2.mobbattlemusic.client.sound.MobBattleTrack;
import nonamecrackers2.mobbattlemusic.client.sound.track.TrackType;
import nonamecrackers2.mobbattlemusic.client.util.MobBattleMusicCompat;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;
import nonamecrackers2.mobbattlemusic.mixin.MixinAbstractSoundInstance;
import nonamecrackers2.mobbattlemusic.mixin.MixinMusicManagerAccessor;
import nonamecrackers2.mobbattlemusic.mixin.MixinSoundEngineAccessor;
import nonamecrackers2.mobbattlemusic.mixin.MixinSoundManagerAccessor;

public class BattleMusicManager
{
	private static final Logger LOGGER = LogManager.getLogger("mobbattlemusic/BattleMusicManager");
	public static final SoundSource DEFAULT_SOUND_SOURCE = SoundSource.RECORDS;
	private static final Predicate<LivingEntity> COUNTS_TOWARDS_MOB_COUNT = e -> {
		return e instanceof Enemy && (MobBattleMusicConfig.CLIENT.whiteListMode.get() ? blacklist(e) : !blacklist(e));
	};
	private final TargetingConditions targetingConditions = TargetingConditions.forCombat().ignoreLineOfSight().range(MobBattleMusicConfig.CLIENT.maxMobSearchRadius.get());
	private final TargetingConditions panicConditions = this.targetingConditions.copy().range(MobBattleMusicConfig.CLIENT.threatRadius.get());
	private final Minecraft minecraft;
	private final ClientLevel level;
	private final Map<TrackType, MobBattleTrack> tracks = Maps.newHashMap();
	private @Nullable TrackType priorityTrack;
	private int maxThreatRefreshTime;
	private int threatRefreshTimer;
	private int threatRemovalTimer;
	private @Nullable LivingEntity panickingFrom;
	
	public BattleMusicManager(Minecraft mc, ClientLevel level)
	{
		this.minecraft = mc;
		this.level = level;
	}
	
	private static boolean blacklist(LivingEntity e)
	{
		return MobBattleMusicConfig.CLIENT.ignoredMobs.get().stream().anyMatch(s -> s.equals(e.getEncodeId()));
	}
	
	public void tick()
	{
		//Determine if the current threat we are panicking from is still valid
		boolean flag = true;
		if (this.panickingFrom != null)
		{
			if (this.panickingFrom.isAlive() && this.panicConditions.test(this.minecraft.player, this.panickingFrom) && this.minecraft.player.hasLineOfSight(this.panickingFrom))
				flag = false;
		}
		//If the threat is no longer valid
		if (flag)
		{
			//No longer panic from that threat after a specified duration of time. This give the sense, that even
			//though the threat is gone, the player needs to "calm" down before the aggressive music track
			//will stop playing
			if (this.threatRemovalTimer++ > MobBattleMusicConfig.CLIENT.calmDownTime.get() * 20)
			{
				this.threatRemovalTimer = 0;
				this.panickingFrom = null;
			}
			this.threatRefreshTimer = this.maxThreatRefreshTime;
		}
		else //If the threat is still valid
		{
			this.threatRemovalTimer = 0;
			//After a certain length of time we will "reevaluate" our current threat by setting it to null.
			//If the threat is still a threat, our current threat will be re-set to that that threat.
			//This is mostly used for players who stopped attacking us. Otherwise, the mod will still consider them a threat
			//and play the player music track even though the player has stopped attacking
			if (this.threatRefreshTimer > 0)
			{
				this.threatRefreshTimer--;
				if (this.threatRefreshTimer == 0)
					this.panickingFrom = null;
			}
		}
		
		MobSelection.Builder builder = MobSelection.builder();
		for (Mob mob : this.level.getNearbyEntities(Mob.class, this.targetingConditions, this.minecraft.player, this.minecraft.player.getBoundingBox().inflate(MobBattleMusicConfig.CLIENT.maxMobSearchRadius.get())))
		{
			if (COUNTS_TOWARDS_MOB_COUNT.test(mob))
			{
				boolean viewable = this.minecraft.levelRenderer.getFrustum().isVisible(mob.getBoundingBox());
				boolean lineOfSight = this.minecraft.player.hasLineOfSight(mob);
				if (this.isMobAggressive(mob))
				{
					builder.addToGroup(MobSelection.GroupType.ATTACKING, MobSelection.Selector.ANY, mob);
					if (lineOfSight)
					{
						builder.addToGroup(MobSelection.GroupType.ATTACKING, MobSelection.Selector.LINE_OF_SIGHT, mob);
						if (viewable)
							builder.addToGroup(MobSelection.GroupType.ATTACKING, MobSelection.Selector.ON_SCREEN, mob);
					}
				}
				builder.addToGroup(MobSelection.GroupType.ENEMIES, MobSelection.Selector.ANY, mob);
				if (lineOfSight)
				{
					builder.addToGroup(MobSelection.GroupType.ENEMIES, MobSelection.Selector.LINE_OF_SIGHT, mob);
					if (viewable)
						builder.addToGroup(MobSelection.GroupType.ENEMIES, MobSelection.Selector.ON_SCREEN, mob);
				}
			}
		}
		MobSelection selection = builder.setPanicTarget(this.panickingFrom).build();
		
		Mob closestAggressor = null;
		double distance = -1.0D;
		for (Mob mob : selection.group(MobSelection.GroupType.ATTACKING).forSelector(MobSelection.defaultSelector()))
		{
			double d = mob.distanceTo(this.minecraft.player);
			if (distance == -1.0D || distance > d)
			{
				closestAggressor = mob;
				distance = d;
			}
		}
		if (closestAggressor != null && this.panickingFrom != closestAggressor && this.panicConditions.test(this.minecraft.player, closestAggressor) && !(this.panickingFrom instanceof Player))
			this.panic(closestAggressor, MobBattleMusicConfig.CLIENT.threatReevaluationCooldown.get() * 20);
		
		
		var iterator = this.tracks.entrySet().iterator();
		while (iterator.hasNext())
		{
			var entry = iterator.next();
			MobBattleTrack track = entry.getValue();
			if (track.isStopped() || !this.minecraft.getSoundManager().isActive(track))
			{
				LOGGER.debug("Removing track {}, it is no longer playing", track);
				iterator.remove();
			}
		}
		
		List<TrackType> tracks = MusicTracksManager.getInstance().getTracks();
		
		TrackType priority = null;
		for (TrackType type : tracks)
		{
			if (type.canPlay(selection))
			{
				priority = type;
				break;
			}
		}
		this.priorityTrack = priority;
		
		for (TrackType type : tracks)
		{
			float trackDesiredVolume = shouldStopTracksForModCompat(this.minecraft.getSoundManager()) ? 0.0F : type.getVolume(selection);
			boolean canPlay = priority == type;
			this.initiateAndOrUpdateTrack(type, canPlay && trackDesiredVolume > 0.0F, track -> 
			{
				float volume = 0.0F;
				if (canPlay)
					volume = trackDesiredVolume;
				track.setTargetedVolume(volume);
			});
		}
		
		if (this.isPlaying())
			fadeAndStopMinecraftMusic(this.minecraft.getMusicManager());
	}
	
	private boolean isMobAggressive(Mob mob)
	{
		if (mob instanceof Warden warden)
			return warden.getClientAngerLevel() > 50;
		else if (mob instanceof Witch)
			return mob.distanceTo(this.minecraft.player) < 12.0D;
		else if (mob instanceof Blaze blaze)
			return blaze.isOnFire();
		else if (mob instanceof AbstractIllager illager)
			return illager.getArmPose() == AbstractIllager.IllagerArmPose.ATTACKING || illager.getArmPose() == AbstractIllager.IllagerArmPose.SPELLCASTING || illager.getArmPose() == AbstractIllager.IllagerArmPose.CROSSBOW_HOLD || illager.getArmPose() == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
		else
			return mob.isAggressive();
	}
	
	private void initiateAndOrUpdateTrack(TrackType type, boolean allowNewTracks, Consumer<MobBattleTrack> consumer)
	{
		MobBattleTrack track;
		if (allowNewTracks && this.minecraft.options.getSoundSourceVolume(BattleMusicManager.DEFAULT_SOUND_SOURCE) > 0.0F && this.minecraft.options.getSoundSourceVolume(SoundSource.MASTER) > 0.0F)
		{
			track = this.tracks.computeIfAbsent(type, t -> {
				var tr = new MobBattleTrack(type.getTrack(), type.getFadeTime());
				this.minecraft.getSoundManager().queueTickingSound(tr);
				LOGGER.debug("Beginning track {}", type);
				return tr;
			});
		}
		else
		{
			track = this.tracks.get(type);
		}
		if (track != null)
			consumer.accept(track);
	}
	
	public void wasAttacked(DamageSource source)
	{
		Entity entity = source.getEntity();
		if (entity instanceof Mob mob && entity instanceof Enemy && !(this.panickingFrom instanceof Player))
			this.panic(mob, MobBattleMusicConfig.CLIENT.threatReevaluationCooldown.get() * 20);
		else if (entity instanceof Player player && player != this.minecraft.player && (MobBattleMusicConfig.CLIENT.punchingCountsAsViolence.get() || (player.getMainHandItem().getItem() instanceof TieredItem || source.getDirectEntity() instanceof Projectile)))
			this.panic(player, MobBattleMusicConfig.CLIENT.playerReevaluationCooldown.get() * 20);
	}
	
	public void onAttack(Entity entity)
	{
		if (entity instanceof Mob mob && entity instanceof Enemy && !(this.panickingFrom instanceof Player))
			this.panic(mob, MobBattleMusicConfig.CLIENT.threatReevaluationCooldown.get() * 20);
		else if (entity instanceof Player player && !player.isCreative() && player != this.minecraft.player && (MobBattleMusicConfig.CLIENT.punchingCountsAsViolence.get() || this.minecraft.player.getMainHandItem().getItem() instanceof TieredItem))
			this.panic(player, MobBattleMusicConfig.CLIENT.playerReevaluationCooldown.get() * 20);
	}
	
	private void panic(LivingEntity mob, int time)
	{
		this.panickingFrom = mob;
		this.threatRefreshTimer = time;
		this.maxThreatRefreshTime = time;
	}
	
	public void reload()
	{
		var iterator = this.tracks.values().iterator();
		while (iterator.hasNext())
		{
			var track = iterator.next();
			track.stop();
			iterator.remove();
			LOGGER.debug("Reloading mob battle music");
		}
	}
	
	public boolean isPlaying()
	{
		return !this.tracks.isEmpty();
	}
	
	public @Nullable TrackType getPriorityTrack()
	{
		return this.priorityTrack;
	}
	
	public List<TrackType> getPlayingTracks()
	{
		return ImmutableList.copyOf(this.tracks.keySet());
	}
	
	public @Nullable LivingEntity getPanicTarget()
	{
		return this.panickingFrom;
	}
	
	private static void fadeAndStopMinecraftMusic(MusicManager manager)
	{
		SoundInstance currentMusic = ((MixinMusicManagerAccessor)manager).mobbattlemusic$getCurrentMusic();
		if (currentMusic instanceof AbstractSoundInstance)
		{
			MixinAbstractSoundInstance mixinCurrentMusic = (MixinAbstractSoundInstance)currentMusic;
			if (currentMusic.getVolume() > 0.0F)
			{
				mixinCurrentMusic.mobbattlemusic$setVolume(mixinCurrentMusic.mobbattlemusic$getVolume() - 0.01F);
				if (currentMusic.getVolume() <= 0.0F)
					manager.stopPlaying();
			}
		}
	}
	
	private static boolean shouldStopTracksForModCompat(SoundManager manager)
	{
		SoundEngine engine = ((MixinSoundManagerAccessor)manager).mobbattlemusic$getSoundEngine();
		MixinSoundEngineAccessor accessor = (MixinSoundEngineAccessor)engine;
		for (SoundInstance sound : accessor.mobbattlemusic$getInstanceToChannel().keySet())
		{
			var clazz = MobBattleMusicCompat.getWitherStormModBossThemeLoopClass();
			if (clazz != null && clazz.isAssignableFrom(sound.getClass()))
				return true;
		}
		return false;
	}
}
