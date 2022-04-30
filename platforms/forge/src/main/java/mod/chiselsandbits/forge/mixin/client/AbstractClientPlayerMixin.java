package mod.chiselsandbits.forge.mixin.client;

import mod.chiselsandbits.client.logic.IsScopingHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin
{

    @Inject(
      method = "getFieldOfViewModifier",
      at = @At(
        value = "RETURN",
        ordinal = 0
      ),
      cancellable = true
    )
    public void onGetFieldOfViewModifier(
      final CallbackInfoReturnable<Float> cir
    )
    {
        if (IsScopingHandler.isScoping()) {
            cir.setReturnValue(0.4F);
        }
    }

}
