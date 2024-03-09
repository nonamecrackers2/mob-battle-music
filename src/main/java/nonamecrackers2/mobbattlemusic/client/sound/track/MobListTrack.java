package nonamecrackers2.mobbattlemusic.client.sound.track;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;

public class MobListTrack extends MobTrack
{
	private final List<EntityType<?>> types;
	
	public MobListTrack(List<EntityType<?>> types, ResourceLocation track, int fadeTime, MobSelection.GroupType group, MobSelection.Selector selector)
	{
		super(track, fadeTime, group, selector);
		this.types = types;
	}
	
	@Override
	public boolean canPlay(MobSelection selection)
	{
		return selection.panicTarget() != null && this.types.contains(selection.panicTarget().getType()) || this.getMobs(selection).stream().anyMatch(m -> this.types.contains(m.getType()));
	}
}
