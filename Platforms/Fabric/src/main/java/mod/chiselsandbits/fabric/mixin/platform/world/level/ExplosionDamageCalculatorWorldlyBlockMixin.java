package mod.chiselsandbits.fabric.mixin.platform.world.level;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExplosionDamageCalculator.class)
public abstract class ExplosionDamageCalculatorWorldlyBlockMixin
{

    @Redirect(
      method = "getBlockExplosionResistance",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/Block;getExplosionResistance()F"
      )
    )
    public float redirectGetBlockStateSoundType(Block block, Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getExplosionResistance(
              blockState, blockGetter, blockPos, explosion
            );
        }
        return block.getExplosionResistance();
    }
}
