package mod.chiselsandbits.fabric.mixin;

import com.communi.suggestu.scena.core.entity.block.IBlockEntityPositionManager;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChiseledBlockEntity.class)
public abstract class MixinChiseledBlockEntity {

    @Redirect(method = "setLevel", at = @At(value = "INVOKE", target = "Lcom/communi/suggestu/scena/core/entity/block/IBlockEntityPositionManager;add(Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void chiselsandbits$disable(IBlockEntityPositionManager instance, BlockEntity blockEntity){
        // IBlockEntityPositionManager#add causes deadlock on fabric
        // So ignoring IBlockEntityPositionManager#add call
    }
}
