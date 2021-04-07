package mod.chiselsandbits.client.model.baked.chiseled;

import net.minecraft.block.BlockState;
import java.util.function.Predicate;

public enum VoxelType
{
    SOLID(s -> s.getFluidState().isEmpty()),
    FLUID(s -> !s.getFluidState().isEmpty()),
    UNKNOWN(s -> true);

    private final Predicate<BlockState> isValidBlockStateCallback;

    VoxelType(final Predicate<BlockState> isValidBlockStateCallback) {this.isValidBlockStateCallback = isValidBlockStateCallback;}

    public boolean isValidBlockState(final BlockState blockState) {
        return this.isValidBlockStateCallback.test(blockState);
    }
}
