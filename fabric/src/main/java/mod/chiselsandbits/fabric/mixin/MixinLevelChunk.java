package mod.chiselsandbits.fabric.mixin;

import com.communi.suggestu.scena.fabric.platform.entity.IFabricBlockEntityPositionHolder;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk {

    // Fix https://github.com/ChiselsAndBits/Chisels-and-Bits/issues/1216
    @Inject(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;setLevel(Lnet/minecraft/world/level/Level;)V"))
    private void mod$manuallyAddToHolder(BlockEntity blockEntity, CallbackInfo ci){
        if(! (blockEntity instanceof ChiseledBlockEntity))
            return;

        IFabricBlockEntityPositionHolder holder = (IFabricBlockEntityPositionHolder) this;
        holder.scena$add(blockEntity.getClass(), blockEntity.getBlockPos());
    }
}
