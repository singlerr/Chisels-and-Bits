package mod.chiselsandbits.client.model.baked.chiseled;

import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.Predicate;

public enum VoxelType
{
    SOLID(s -> s.getFluidState().isEmpty()),
    FLUID(s -> !s.getFluidState().isEmpty()),
    UNKNOWN(s -> true);

    private final Predicate<BlockState> isValidBlockStateCallback;

    VoxelType(final Predicate<BlockState> isValidBlockStateCallback) {this.isValidBlockStateCallback = isValidBlockStateCallback;}

    public boolean isValidBlockState(final IBlockInformation blockState) {
        return this.isValidBlockStateCallback.test(blockState.getBlockState());
    }

    public boolean isFluid() {
        return this == FLUID;
    }
}
