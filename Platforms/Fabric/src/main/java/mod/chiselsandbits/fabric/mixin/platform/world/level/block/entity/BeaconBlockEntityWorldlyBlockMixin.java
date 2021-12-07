package mod.chiselsandbits.fabric.mixin.platform.world.level.block.entity;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityWorldlyBlockMixin extends BlockEntity
{

    private static final ThreadLocal<BlockPos> lastRequestedBlockPosHolder = new ThreadLocal<>();

    public BeaconBlockEntityWorldlyBlockMixin(final BlockEntityType<?> blockEntityType, final BlockPos blockPos, final BlockState blockState)
    {
        super(blockEntityType, blockPos, blockState);
    }

    @Redirect(
      method = "tick",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
      )
    )
    private static BlockState redirectGetLevelGetBlockState(Level level, BlockPos blockPos)
    {
        lastRequestedBlockPosHolder.set(blockPos);
        return level.getBlockState(blockPos);
    }

    @Redirect(
      method = "tick",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/item/DyeColor;getTextureDiffuseColors()[F"
      )
    )
    private static float[] redirectGetDyeColorGetTextureDiffuseColors(DyeColor dyeColor, Level level, BlockPos beaconPosition)
    {
        final BlockState blockState = level.getBlockState(lastRequestedBlockPosHolder.get());
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties) {
            return blockWithWorldlyProperties.getBeaconColorMultiplier(
              blockState, level, lastRequestedBlockPosHolder.get(), beaconPosition
            );
        }

        return dyeColor.getTextureDiffuseColors();
    }
}
