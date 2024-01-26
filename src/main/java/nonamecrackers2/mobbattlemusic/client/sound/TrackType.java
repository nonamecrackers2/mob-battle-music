package nonamecrackers2.mobbattlemusic.client.sound;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;
import nonamecrackers2.mobbattlemusic.common.init.MobBattleMusicSoundEvents;

public enum TrackType
{
	NON_AGGRESSIVE(MobBattleMusicSoundEvents.NON_AGGRO_TRACK, 120),
	AGGRESSIVE(MobBattleMusicSoundEvents.AGGRO_TRACK, 40),
	PLAYER(MobBattleMusicSoundEvents.PLAYER_TRACK, 20);
	
	private final Supplier<SoundEvent> track;
	private final int fadeTime;
	
	private TrackType(Supplier<SoundEvent> track, int fadeTime)
	{ 
		this.track = track;
		this.fadeTime = fadeTime;
	}
	
	public SoundEvent getTrack()
	{
		return this.track.get();
	}
	
	public int getFadeTime()
	{
		return this.fadeTime;
	}
}
