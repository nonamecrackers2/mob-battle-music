package nonamecrackers2.mobbattlemusic.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.Multimap;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import nonamecrackers2.mobbattlemusic.client.sound.MobBattleTrack;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine
{
	@Shadow
	private Map<SoundInstance, Integer> soundDeleteTime;
	@Shadow
	private Multimap<SoundSource, SoundInstance> instanceBySource;
	@Shadow
	private List<TickableSoundInstance> tickingSounds;
	
	//Minecraft likes to just clear sounds when it's SoundSource volume is at 0 and not properly clear things. This mixin properly clears things, but
	//only for custom SoundInstances implemented in this mod just to make sure we don't break anything else.
	@Inject(method = "tickNonPaused", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	public void mobbattlemusic$properlyClearTrack_tickNonPaused(CallbackInfo ci, Iterator<Map.Entry<SoundInstance, ChannelAccess.ChannelHandle>> iterator, Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry, ChannelAccess.ChannelHandle channelaccess$channelhandle1, SoundInstance soundinstance)
	{
		if (soundinstance instanceof MobBattleTrack track)
		{
			this.soundDeleteTime.remove(soundinstance);
			try {
				this.instanceBySource.remove(soundinstance.getSource(), soundinstance);
			} catch (RuntimeException runtimeexception){
			}
			this.tickingSounds.remove(track);
		}
	}
}
