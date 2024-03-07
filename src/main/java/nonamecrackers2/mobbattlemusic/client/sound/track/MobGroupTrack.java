package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class MobGroupTrack extends TrackType
{
	private final TagKey<EntityType<?>> tag;
	private final int fadeTime;
	
	public MobGroupTrack(TagKey<EntityType<?>> tag, ResourceLocation track, int fadeTime)
	{
		super(track);
		this.tag = tag;
		this.fadeTime = fadeTime;
	}
	
	@Override
	public boolean canPlay(LivingEntity panickingFrom, int enemyCount, int aggroCount)
	{
		return panickingFrom != null && panickingFrom.getType().is(this.tag);
	}
	
	@Override
	public int getFadeTime()
	{
		return this.fadeTime;
	}
}
