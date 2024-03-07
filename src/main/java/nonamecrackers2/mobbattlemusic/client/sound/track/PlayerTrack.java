package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;
import nonamecrackers2.mobbattlemusic.client.sound.MobBattleMusicSounds;

public class PlayerTrack extends TrackType
{
	protected PlayerTrack()
	{
		super(MobBattleMusicSounds.PLAYER_TRACK);
	}
	
	@Override
	public boolean canPlay(LivingEntity panickingFrom, int enemyCount, int aggroCount)
	{
		return MobBattleMusicConfig.CLIENT.playerTrackEnabled.get() && panickingFrom instanceof Player;
	}
	
	@Override
	public int getFadeTime()
	{
		return (int)(MobBattleMusicConfig.CLIENT.playerFadeTime.get() * 20.0D);
	}
}
