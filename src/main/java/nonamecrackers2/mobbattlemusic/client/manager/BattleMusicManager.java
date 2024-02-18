package nonamecrackers2.mobbattlemusic.client.manager;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.TieredItem;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;
import nonamecrackers2.mobbattlemusic.client.sound.MobBattleTrack;
import nonamecrackers2.mobbattlemusic.client.sound.TrackType;
import nonamecrackers2.mobbattlemusic.client.util.MobBattleMusicCompat;
import nonamecrackers2.mobbattlemusic.mixin.MixinAbstractSoundInstance;
import nonamecrackers2.mobbattlemusic.mixin.MixinMusicManagerAccessor;
import nonamecrackers2.mobbattlemusic.mixin.MixinSoundEngineAccessor;
import nonamecrackers2.mobbattlemusic.mixin.MixinSoundManagerAccessor;

public class BattleMusicManager
{
	private static final Logger LOGGER = LogManager.getLogger("mobbattlemusic/BattleMusicManager");
	public static final SoundSource DEFAULT_SOUND_SOURCE = SoundSource.RECORDS;
	private static final Predicate<LivingEntity> COUNTS_TOWARDS_MOB_COUNT = e -> {
		return e instanceof Enemy && !MobBattleMusicConfig.CLIENT.ignoredMobs.get().stream().anyMatch(s -> s.equals(e.getEncodeId()));
	};
	private final TargetingConditions targetingConditions = TargetingConditions.forCombat().ignoreLineOfSight().range(MobBattleMusicConfig.CLIENT.maxMobSearchRadius.get());
	private final TargetingConditions panicConditions = this.targetingConditions.copy().range(MobBattleMusicConfig.CLIENT.threatRadius.get());
	private final Minecraft minecraft;
	private final ClientLevel level;
	private final Map<TrackType, MobBattleTrack> tracks = Maps.newEnumMap(TrackType.class);
	private int panicTicks;
	private int panicRefreshTime;
	private @Nullable LivingEntity panickingFrom;
	
	public BattleMusicManager(Minecraft mc, ClientLevel level)
	{
		this.minecraft = mc;
		this.level = level;
	}
	
	public void tick()
	{
		boolean flag = true;
		if (this.panickingFrom != null)
		{
			if (this.panickingFrom.isAlive())
			{
				if (this.panicConditions.test(this.minecraft.player, this.panickingFrom) && this.minecraft.player.hasLineOfSight(this.panickingFrom))
					flag = false;
			}
			else
			{
				this.panickingFrom = null;
			}
		}
		if (flag)
		{
			if (this.panicTicks > 0)
			{
				this.panicTicks--;
				if (this.panicTicks == 0)
					this.panickingFrom = null;
			}
			if (this.panicRefreshTime > 0)
				this.panicRefreshTime--;
		}
		else
		{
			this.panicTicks = MobBattleMusicConfig.CLIENT.aggressiveCooldown.get() * 20;
			this.panicRefreshTime++;
			if (this.panicRefreshTime > 200)
			{
				this.panickingFrom = null;
				this.panicRefreshTime = 0;
			}
		}
//		System.out.println(this.panickingFrom);
//		System.out.println(this.panicTicks);
//		System.out.println(this.panicRefreshTime);
		
		int enemiesCount = 0;
		int aggroCount = 0;
		Mob closestAggressor = null;
		double distance = -1.0D;
		for (Mob mob : this.level.getNearbyEntities(Mob.class, this.targetingConditions, this.minecraft.player, this.minecraft.player.getBoundingBox().inflate(MobBattleMusicConfig.CLIENT.maxMobSearchRadius.get())))
		{
			if (COUNTS_TOWARDS_MOB_COUNT.test(mob) && this.minecraft.player.hasLineOfSight(mob))
			{
				if (this.isMobAggressive(mob) && (!MobBattleMusicConfig.CLIENT.onlyCountVisibleMobs.get() || this.minecraft.levelRenderer.getFrustum().isVisible(mob.getBoundingBox())))
				{
					double d = mob.distanceTo(this.minecraft.player);
					if (distance == -1.0D || distance > d)
					{
						closestAggressor = mob;
						distance = d;
					}
					aggroCount++;
				}
				enemiesCount++;
			}
		}
		if (closestAggressor != null && this.panicConditions.test(this.minecraft.player, closestAggressor) && !(this.panickingFrom instanceof Player))
			this.panickingFrom = closestAggressor;
		
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
		
		TrackType priority = this.getPriorityTrack(enemiesCount, aggroCount);
		for (TrackType type : TrackType.values())
		{
			float trackDesiredVolume = shouldStopTracksForModCompat(this.minecraft.getSoundManager()) ? 0.0F : getTrackVolume(type, enemiesCount, aggroCount);
			boolean canPlay = type.canPlay();
			this.initiateAndOrUpdateTrack(type, priority == type && trackDesiredVolume > 0.0F && canPlay, track -> 
			{
				float volume = 0.0F;
				if (canPlay && type == priority)
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
			this.panic(mob, MobBattleMusicConfig.CLIENT.aggressiveCooldown.get() * 20);
		else if (entity instanceof Player player && player != this.minecraft.player && (player.getMainHandItem().getItem() instanceof TieredItem || source.getDirectEntity() instanceof Projectile))
			this.panic(player, MobBattleMusicConfig.CLIENT.aggressivePlayerCooldown.get() * 20);
	}
	
	private void panic(LivingEntity mob, int time)
	{
		this.panickingFrom = mob;
		this.panicTicks = time;
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
	
	private @Nullable TrackType getPriorityTrack(int enemyCount, int aggroCount)
	{
		if (this.panickingFrom instanceof Player)
			return TrackType.PLAYER;
		else if (aggroCount > 0 || this.panicTicks > 0)
			return TrackType.AGGRESSIVE;
		else if (enemyCount > 0)
			return TrackType.NON_AGGRESSIVE;
		else
			return null;
	}
	
	private static float getTrackVolume(TrackType type, int enemyCount, int aggroCount)
	{
		switch (type)
		{
		case NON_AGGRESSIVE:
			return Mth.clamp((float)enemyCount / (float)MobBattleMusicConfig.CLIENT.maxMobsForMaxVolume.get(), 0.0F, 1.0F);
		default:
			return 1.0F;
		}
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
