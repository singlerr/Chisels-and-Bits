package mod.chiselsandbits.fabric.mixin.platform.world.level;

import mod.chiselsandbits.fabric.platform.level.FabricLevelBasedPropertyAccessor;
import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelWorldlyBlockMixin implements LevelAccessor, AutoCloseable
{

    private Level getInternalMixinTarget() {
        return (Level) (Object) this;
    }

    @Shadow public abstract int getDirectSignalTo(final BlockPos param0);

    @Inject(
      method = "getSignal",
             at = @At("HEAD"),
             cancellable = true
    )
    public void checkForWorldlySignalBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        final BlockState blockState = getInternalMixinTarget().getBlockState(blockPos);
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties) {
            final boolean shouldCheck = blockWithWorldlyProperties.shouldCheckWeakPower(
              blockState, getInternalMixinTarget(), blockPos, direction
            );

            final int signal = blockState.getSignal(this, blockPos, direction);

            cir.setReturnValue(
                shouldCheck ? Math.max(signal, this.getDirectSignalTo(blockPos)) : signal
            );
        }
    }

    @Redirect(
      method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"
      )
    )
    public int redirectGetBlockStateLightEmission(BlockState blockState, final BlockPos blockPos)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getLightEmission(
              blockState, getInternalMixinTarget(), blockPos
            );
        }
        return blockState.getLightEmission();
    }
}
