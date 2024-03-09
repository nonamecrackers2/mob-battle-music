package nonamecrackers2.mobbattlemusic.client.sound.track;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;

public abstract class MobTrack extends TrackType
{
	private final int fadeTime;
	protected final MobSelection.GroupType group;
	protected final MobSelection.Selector selector;
	
	public MobTrack(ResourceLocation track, int fadeTime, MobSelection.GroupType group, MobSelection.Selector selector)
	{
		super(track);
		this.fadeTime = fadeTime;
		this.group = group;
		this.selector = selector;
	}
	
	protected List<Mob> getMobs(MobSelection selection)
	{
		return selection.group(this.group).forSelector(this.selector);
	}
	
	@Override
	public int getFadeTime()
	{
		return this.fadeTime;
	}
}
