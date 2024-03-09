package nonamecrackers2.mobbattlemusic.client.sound.track;

import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;
import nonamecrackers2.mobbattlemusic.client.sound.MobBattleMusicSounds;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;

public class AggressiveTrack extends TrackType
{
	protected AggressiveTrack()
	{
		super(MobBattleMusicSounds.AGGRO_TRACK);
	}
	
	@Override
	public boolean canPlay(MobSelection selection)
	{
		return MobBattleMusicConfig.CLIENT.aggressiveTrackEnabled.get() && (selection.group(MobSelection.GroupType.ATTACKING).count(MobSelection.defaultSelector()) > 0 || selection.panicTarget() != null);
	}
	
	@Override
	public int getFadeTime()
	{
		return (int)(MobBattleMusicConfig.CLIENT.aggressiveFadeTime.get() * 20.0D);
	}
}
