package nonamecrackers2.mobbattlemusic.client.sound;

import com.google.common.base.Supplier;

import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;

public enum TrackType
{
	NON_AGGRESSIVE(MobBattleMusicSounds.NON_AGGRO_TRACK, () -> (int)(MobBattleMusicConfig.CLIENT.nonAggressiveFadeTime.get() * 20.0D), MobBattleMusicConfig.CLIENT.nonAggressiveTrackEnabled::get),
	AGGRESSIVE(MobBattleMusicSounds.AGGRO_TRACK, () -> (int)(MobBattleMusicConfig.CLIENT.aggressiveFadeTime.get() * 20.0D), MobBattleMusicConfig.CLIENT.aggressiveTrackEnabled::get),
	PLAYER(MobBattleMusicSounds.PLAYER_TRACK, () -> (int)(MobBattleMusicConfig.CLIENT.playerFadeTime.get() * 20.0D), MobBattleMusicConfig.CLIENT.playerTrackEnabled::get);
	
	private final ResourceLocation track;
	private final Supplier<Integer> fadeTime;
	private final Supplier<Boolean> canPlay;
	
	private TrackType(ResourceLocation track, Supplier<Integer> fadeTime, Supplier<Boolean> canPlay)
	{ 
		this.track = track;
		this.fadeTime = fadeTime;
		this.canPlay = canPlay;
	}
	
	public ResourceLocation getTrack()
	{
		return this.track;
	}
	
	public int getFadeTime()
	{
		return this.fadeTime.get();
	}
	
	public boolean canPlay()
	{
		return this.canPlay.get();
	}
}
