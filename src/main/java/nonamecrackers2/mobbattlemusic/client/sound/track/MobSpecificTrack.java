package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;

public class MobSpecificTrack extends TrackType
{
	private final EntityType<?> type;
	private final int fadeTime;
	private final MobSelection.Type selector;
	
	public MobSpecificTrack(EntityType<?> type, ResourceLocation track, int fadeTime, MobSelection.Type selector)
	{
		super(track);
		this.type = type;
		this.fadeTime = fadeTime;
		this.selector = selector;
	}
	
	@Override
	public boolean canPlay(MobSelection selection)
	{
		return selection.panicTarget() != null && selection.panicTarget().getType() == this.type || selection.forSelection(this.selector).stream().anyMatch(m -> m.getType() == this.type);
	}
	
	@Override
	public int getFadeTime()
	{
		return this.fadeTime;
	}
}
