package nonamecrackers2.mobbattlemusic.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import nonamecrackers2.mobbattlemusic.client.manager.BattleMusicManager;

public class MobBattleTrack extends AbstractTickableSoundInstance
{
	public static final int MAX_EMPTY_TIME = 300;
	private final int fadeTime;
	private float targetedVolume = 1.0F;
	private int emptyTime;
	
	public MobBattleTrack(SoundEvent event, int fadeTime)
	{
		super(event, BattleMusicManager.DEFAULT_SOUND_SOURCE, SoundInstance.createUnseededRandom());
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
	
	public void forceStop()
	{
		this.stop();
	}
}
