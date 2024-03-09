package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;

public class MobTagTrack extends MobTrack
{
	private final TagKey<EntityType<?>> tag;
	
	public MobTagTrack(TagKey<EntityType<?>> tag, ResourceLocation track, int fadeTime, MobSelection.GroupType group, MobSelection.Selector selector)
	{
		super(track, fadeTime, group, selector);
		this.tag = tag;
	}
	
	@Override
	public boolean canPlay(MobSelection selection)
	{
		return selection.panicTarget() != null && selection.panicTarget().getType().is(this.tag) || this.getMobs(selection).stream().anyMatch(m -> m.getType().is(this.tag));
	}
}
