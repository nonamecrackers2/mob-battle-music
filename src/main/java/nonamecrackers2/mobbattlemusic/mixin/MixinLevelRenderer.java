package nonamecrackers2.mobbattlemusic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;

@Mixin(LevelRenderer.class)
public interface MixinLevelRenderer
{
	@Accessor("cullingFrustum")
	Frustum mobbattlemusic$getCullingFrustum();
	
	@Accessor("capturedFrustum")
	Frustum mobbattlemusic$getCapturedFrustum();
}
