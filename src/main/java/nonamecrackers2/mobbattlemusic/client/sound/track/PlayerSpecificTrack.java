package nonamecrackers2.mobbattlemusic.client.sound.track;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;

public class PlayerSpecificTrack extends TrackType
{
	private final String name;
	private final boolean uuidMode;
	private final int fadeTime;
	
	public PlayerSpecificTrack(String name, boolean uuidMode, ResourceLocation track, int fadeTime)
	{
		super(track);
		this.name = name;
		this.uuidMode = uuidMode;
		this.fadeTime = fadeTime;
	}
	
	@Override
	public int getFadeTime()
	{
		return this.fadeTime;
	}
	
	@Override
	public boolean canPlay(MobSelection selection)
	{
		if (this.uuidMode)
			return selection.panicTarget() instanceof Player && selection.panicTarget().getStringUUID().equals(this.name);
		else
			return selection.panicTarget() instanceof Player && selection.panicTarget().getDisplayName().getString().equals(this.name);
	}
	
	@Override
	public String toString()
	{
		return "PlayerSpecificTrack[" + this.getTrack() + ", uuid_mode=" + this.uuidMode + "]";
	}
}
