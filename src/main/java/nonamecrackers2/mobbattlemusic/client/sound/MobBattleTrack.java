package nonamecrackers2.mobbattlemusic.client.sound;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import nonamecrackers2.mobbattlemusic.client.manager.BattleMusicManager;

public class MobBattleTrack extends AbstractSoundInstance implements TickableSoundInstance
{
	public static final int MAX_EMPTY_TIME = 300;
	private final int fadeTime;
	private float targetedVolume = 1.0F;
	private int emptyTime;
	private boolean stopped;
	
	public MobBattleTrack(ResourceLocation sound, int fadeTime)
	{
		super(sound, BattleMusicManager.DEFAULT_SOUND_SOURCE, SoundInstance.createUnseededRandom());
		this.fadeTime = fadeTime;
		this.looping = true;
		this.delay = 0;
		this.volume = 0.0F;
		this.relative = true;
	}

	@Override
	public void tick()
	{
		float delta = (this.targetedVolume - this.volume) / this.fadeTime;
		this.volume += delta;
		
		if (this.emptyTime++ > MAX_EMPTY_TIME)
			this.stop();
		
		if (this.volume > 0.01F)
			this.emptyTime = 0;
	}
	
	public void setTargetedVolume(float volume)
	{
		this.targetedVolume = volume;
	}
	
	@Override
	public boolean canStartSilent()
	{
		return true;
	}
	
	@Override
	public boolean isStopped()
	{
		return this.stopped;
	}
	
	public void stop()
	{
		this.stopped = true;
		this.looping = false;
	}
}
