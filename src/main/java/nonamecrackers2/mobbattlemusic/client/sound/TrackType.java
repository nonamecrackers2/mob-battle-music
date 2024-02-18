package nonamecrackers2.mobbattlemusic.client.sound;

import com.google.common.base.Supplier;

import net.minecraft.resources.ResourceLocation;

public enum TrackType
{
	NON_AGGRESSIVE(MobBattleMusicSounds.NON_AGGRO_TRACK, 120),
	AGGRESSIVE(MobBattleMusicSounds.AGGRO_TRACK, 40),
	PLAYER(MobBattleMusicSounds.PLAYER_TRACK, 20);
	
	private final ResourceLocation track;
	private final Supplier<Integer> fadeTime;
	
	private TrackType(ResourceLocation track, Supplier<Integer> fadeTime)
	{ 
		this.track = track;
		this.fadeTime = fadeTime;
	}
	
	public ResourceLocation getTrack()
	{
		return this.track;
	}
	
	public int getFadeTime()
	{
		return this.fadeTime.get();
	}
}
