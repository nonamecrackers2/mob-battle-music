package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.util.Mth;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;
import nonamecrackers2.mobbattlemusic.client.sound.MobBattleMusicSounds;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;

public class AmbientTrack extends TrackType
{
	protected AmbientTrack()
	{
		super(MobBattleMusicSounds.NON_AGGRO_TRACK);
	}
	
	@Override
	public boolean canPlay(MobSelection selection)
	{
		return MobBattleMusicConfig.CLIENT.nonAggressiveTrackEnabled.get() && selection.group(MobSelection.GroupType.ENEMIES).count(MobSelection.Selector.LINE_OF_SIGHT) > 0;
	}
	
	@Override
	public int getFadeTime()
	{
		return (int)(MobBattleMusicConfig.CLIENT.nonAggressiveFadeTime.get() * 20.0D);
	}
	
	@Override
	public float getVolume(MobSelection selection)
	{
		return Mth.clamp((float)selection.group(MobSelection.GroupType.ENEMIES).count(MobSelection.defaultSelector()) / (float)MobBattleMusicConfig.CLIENT.maxMobsForMaxVolume.get(), 0.0F, 1.0F);
	}
}
