package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;

public class MobSpecificTrack extends MobTrack
{
	private final EntityType<?> type;
	
	public MobSpecificTrack(EntityType<?> type, ResourceLocation track, int fadeTime, MobSelection.GroupType group, MobSelection.Selector selector)
	{
		super(track, fadeTime, group, selector);
		this.type = type;
	}

	@Override
	public boolean canPlay(MobSelection selection)
	{
		return selection.panicTarget() != null && selection.panicTarget().getType() == this.type || this.getMobs(selection).stream().anyMatch(m -> m.getType() == this.type);
	}
}
