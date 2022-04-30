package mod.chiselsandbits.fabric.mixin.client;

import mod.chiselsandbits.client.logic.IsScopingHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerScopingMixin
{

    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    public void onIsUsingItem(final CallbackInfoReturnable<Boolean> ci) {
        if (IsScopingHandler.isScoping()) {
            ci.setReturnValue(true);
        }
    }
}
