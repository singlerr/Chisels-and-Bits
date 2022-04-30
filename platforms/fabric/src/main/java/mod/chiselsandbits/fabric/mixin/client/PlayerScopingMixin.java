package mod.chiselsandbits.fabric.mixin.client;

import mod.chiselsandbits.client.logic.IsScopingHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerScopingMixin
{

    @Inject(method = "isScoping", at = @At("HEAD"), cancellable = true)
    public void onIsScoping(final CallbackInfoReturnable<Boolean> ci) {
        if (IsScopingHandler.isScoping()) {
            ci.setReturnValue(true);
        }
    }
}
