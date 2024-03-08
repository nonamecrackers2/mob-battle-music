package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;

public abstract class TrackType
{
	public static final TrackType AMBIENT = new AmbientTrack();
	public static final TrackType AGGRESSIVE = new AggressiveTrack();
	public static final TrackType PLAYER = new PlayerTrack();
	
	private final ResourceLocation track;
	
	public TrackType(ResourceLocation track)
	{ 
		this.track = track;
	}
	
	public ResourceLocation getTrack()
	{
		return this.track;
	}

	public float getVolume(MobSelection selection)
	{
		return 1.0F;
	}
	
	public abstract int getFadeTime();
	
	public abstract boolean canPlay(MobSelection selection);
	
	@Override
	public String toString()
	{
		return String.format("%s[%s]", this.getClass().getSimpleName(), this.track);
	}
}
