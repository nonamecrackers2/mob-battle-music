package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;
import nonamecrackers2.mobbattlemusic.client.sound.MobBattleMusicSounds;

public class AmbientTrack extends TrackType
{
	protected AmbientTrack()
	{
		super(MobBattleMusicSounds.NON_AGGRO_TRACK);
	}
	
	@Override
	public boolean canPlay(LivingEntity panickingFrom, int enemyCount, int aggroCount)
	{
		return MobBattleMusicConfig.CLIENT.nonAggressiveTrackEnabled.get() && enemyCount > 0;
	}
	
	@Override
	public int getFadeTime()
	{
		return (int)(MobBattleMusicConfig.CLIENT.nonAggressiveFadeTime.get() * 20.0D);
	}
	
	@Override
	public float getVolume(int enemyCount, int aggroCount)
	{
		return Mth.clamp((float)enemyCount / (float)MobBattleMusicConfig.CLIENT.maxMobsForMaxVolume.get(), 0.0F, 1.0F);
	}
}
