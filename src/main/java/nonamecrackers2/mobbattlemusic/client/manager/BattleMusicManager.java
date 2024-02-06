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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.TieredItem;
import nonamecrackers2.mobbattlemusic.client.sound.MobBattleTrack;
import nonamecrackers2.mobbattlemusic.client.sound.TrackType;
import nonamecrackers2.mobbattlemusic.mixin.MixinAbstractSoundInstance;
import nonamecrackers2.mobbattlemusic.mixin.MixinMusicManagerAccessor;

public class BattleMusicManager
{
	private static final Logger LOGGER = LogManager.getLogger();
	public static final SoundSource DEFAULT_SOUND_SOURCE = SoundSource.RECORDS;
	public static final int SEARCH_RADIUS = 64;
	public static final int PANIC_RADIUS = 24;
	public static final int MAX_ENTITIES_FOR_MAX_VOLUME = 5;
	public static final int MAX_PANIC_TIME = 100;
	public static final int MAX_PANIC_TIME_PLAYER = 400;
	private static final Predicate<LivingEntity> COUNTS_TOWARDS_MOB_COUNT = e -> {
		return e instanceof Enemy && !(e instanceof Creeper);
	};
	private static final TargetingConditions TARGETING_CONDITIONS = TargetingConditions.forCombat().ignoreLineOfSight().range(SEARCH_RADIUS);
	private static final TargetingConditions PANIC_CONDITIONS = TARGETING_CONDITIONS.copy().range(PANIC_RADIUS);
	private final Minecraft minecraft;
	private final ClientLevel level;
	private final Map<TrackType, MobBattleTrack> tracks = Maps.newEnumMap(TrackType.class);
	private int panicTicks;
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
				if (PANIC_CONDITIONS.test(this.minecraft.player, this.panickingFrom) && this.minecraft.player.hasLineOfSight(this.panickingFrom))
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
		}
		else
		{
			this.panicTicks = MAX_PANIC_TIME;
		}
		
		int enemiesCount = 0;
		int aggroCount = 0;
		Mob closestAggressor = null;
		double distance = -1.0D;
		for (Mob mob : this.level.getNearbyEntities(Mob.class, TARGETING_CONDITIONS, this.minecraft.player, this.minecraft.player.getBoundingBox().inflate(SEARCH_RADIUS)))
		{
			if (COUNTS_TOWARDS_MOB_COUNT.test(mob) && this.minecraft.player.hasLineOfSight(mob))
			{
				if (this.isMobAggressive(mob) && this.minecraft.levelRenderer.getFrustum().isVisible(mob.getBoundingBox()))
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
		if (closestAggressor != null && PANIC_CONDITIONS.test(this.minecraft.player, closestAggressor) && !(this.panickingFrom instanceof Player))
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
			float trackDesiredVolume = getTrackVolume(type, enemiesCount, aggroCount);
			this.initiateAndOrUpdateTrack(type, priority == type && trackDesiredVolume > 0.0F, track -> 
			{
				float volume = 0.0F;
				if (type == priority)
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
			this.panic(mob, MAX_PANIC_TIME);
		else if (entity instanceof Player player && player != this.minecraft.player && (player.getMainHandItem().getItem() instanceof TieredItem || source.getDirectEntity() instanceof Projectile))
			this.panic(player, MAX_PANIC_TIME_PLAYER);
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
			return Mth.clamp((float)enemyCount / (float)MAX_ENTITIES_FOR_MAX_VOLUME, 0.0F, 1.0F);
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
}
