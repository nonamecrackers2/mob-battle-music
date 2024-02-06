package nonamecrackers2.mobbattlemusic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import nonamecrackers2.mobbattlemusic.client.init.MobBattleMusicClientCapabilities;

@Mixin(MusicManager.class)
public abstract class MixinMusicManager
{
	@Shadow
	private Minecraft minecraft;
	
	@Inject(method = "startPlaying", at = @At("HEAD"), cancellable = true)
	public void mobbattlemusic$preventMusicFromPlaying_startPlaying(Music music, CallbackInfo ci)
	{
		if (this.minecraft.level != null)
		{
			var manager = this.minecraft.level.getCapability(MobBattleMusicClientCapabilities.MUSIC_MANAGER).orElse(null);
			if (manager != null && manager.isPlaying())
				ci.cancel();
		}
	}
}
