package nonamecrackers2.mobbattlemusic.client.manager;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.TieredItem;
import nonamecrackers2.mobbattlemusic.client.sound.MobBattleTrack;
import nonamecrackers2.mobbattlemusic.client.sound.TrackType;

public class BattleMusicManager
{
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
	private final LocalPlayer player;
	private int panicTicks;
	private @Nullable LivingEntity panickingFrom;
	
	public BattleMusicManager(Minecraft mc, ClientLevel level)
	{
		this.minecraft = mc;
		this.level = level;
		this.player = mc.player;
	}
	
	public void tick()
	{
		boolean flag = true;
		if (this.panickingFrom != null)
		{
			if (this.panickingFrom.isAlive())
			{
				if (PANIC_CONDITIONS.test(this.player, this.panickingFrom) && this.player.hasLineOfSight(this.panickingFrom))
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
		for (Mob mob : this.level.getNearbyEntities(Mob.class, TARGETING_CONDITIONS, this.player, this.player.getBoundingBox().inflate(SEARCH_RADIUS)))
		{
			if (COUNTS_TOWARDS_MOB_COUNT.test(mob) && this.player.hasLineOfSight(mob))
			{
				if (mob.isAggressive() && this.minecraft.levelRenderer.getFrustum().isVisible(mob.getBoundingBox()))
				{
					double d = mob.distanceTo(this.player);
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
		if (closestAggressor != null && PANIC_CONDITIONS.test(this.player, closestAggressor) && !(this.panickingFrom instanceof Player))
			this.panickingFrom = closestAggressor;
		
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
		
		var iterator = this.tracks.entrySet().iterator();
		while (iterator.hasNext())
		{
			var entry = iterator.next();
			MobBattleTrack track = entry.getValue();
			if (track.isStopped())
				iterator.remove();
		}
	}
	
	private void initiateAndOrUpdateTrack(TrackType type, boolean allowNewTracks, Consumer<MobBattleTrack> consumer)
	{
		MobBattleTrack track;
		if (allowNewTracks)
		{
			track = this.tracks.computeIfAbsent(type, t -> {
				var tr = new MobBattleTrack(type.getTrack(), type.getFadeTime());
				this.minecraft.getSoundManager().queueTickingSound(tr);
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
		else if (entity instanceof Player player && player != this.player && (player.getMainHandItem().getItem() instanceof TieredItem || source.getDirectEntity() instanceof Projectile))
			this.panic(player, MAX_PANIC_TIME_PLAYER);
	}
	
	private void panic(LivingEntity mob, int time)
	{
		this.panickingFrom = mob;
		this.panicTicks = time;
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
}
