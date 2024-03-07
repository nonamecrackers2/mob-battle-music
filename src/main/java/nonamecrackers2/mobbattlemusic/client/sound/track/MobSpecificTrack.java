package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class MobSpecificTrack extends TrackType
{
	private final EntityType<?> type;
	private final int fadeTime;
	
	public MobSpecificTrack(EntityType<?> type, ResourceLocation track, int fadeTime)
	{
		super(track);
		this.type = type;
		this.fadeTime = fadeTime;
	}
	
	@Override
	public boolean canPlay(LivingEntity panickingFrom, int enemyCount, int aggroCount)
	{
		return panickingFrom != null && panickingFrom.getType() == this.type;
	}
	
	@Override
	public int getFadeTime()
	{
		return this.fadeTime;
	}
}
