package nonamecrackers2.mobbattlemusic.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;

@Mixin(MusicManager.class)
public interface MixinMusicManagerAccessor
{
	@Accessor("currentMusic")
	@Nullable SoundInstance mobbattlemusic$getCurrentMusic();
}
