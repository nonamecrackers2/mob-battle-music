package nonamecrackers2.mobbattlemusic.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import nonamecrackers2.mobbattlemusic.client.init.MobBattleMusicClientCapabilities;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity
{
	private MixinLivingEntity()
	{
		super(null, null);
		throw new UnsupportedOperationException();
	}
	
	@Inject(method = "handleDamageEvent", at = @At("TAIL"))
	public void mobbattlemusic$onEntityDamaged_handleDamageEvent(DamageSource source, CallbackInfo ci)
	{
		if ((Entity)this instanceof LocalPlayer player && player == Minecraft.getInstance().player)
		{
			this.level().getCapability(MobBattleMusicClientCapabilities.MUSIC_MANAGER).ifPresent(manager -> {
				manager.wasAttacked(source);
			});
		}
	}
}
