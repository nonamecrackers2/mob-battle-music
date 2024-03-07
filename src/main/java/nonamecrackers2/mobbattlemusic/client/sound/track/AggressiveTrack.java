package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;
import nonamecrackers2.mobbattlemusic.client.sound.MobBattleMusicSounds;

public class AggressiveTrack extends TrackType
{
	protected AggressiveTrack()
	{
		super(MobBattleMusicSounds.AGGRO_TRACK);
	}
	
	@Override
	public boolean canPlay(LivingEntity panickingFrom, int enemyCount, int aggroCount)
	{
		return MobBattleMusicConfig.CLIENT.aggressiveTrackEnabled.get() && (aggroCount > 0 || panickingFrom != null);
	}
	
	@Override
	public int getFadeTime()
	{
		return (int)(MobBattleMusicConfig.CLIENT.aggressiveFadeTime.get() * 20.0D);
	}
}
