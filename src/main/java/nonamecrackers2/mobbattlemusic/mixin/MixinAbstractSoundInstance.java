package nonamecrackers2.mobbattlemusic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;

@Mixin(AbstractSoundInstance.class)
public interface MixinAbstractSoundInstance
{
	@Accessor("volume")
	void mobbattlemusic$setVolume(float volume);
	
	@Accessor("volume")
	float mobbattlemusic$getVolume();
}
