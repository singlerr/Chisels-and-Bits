package mod.chiselsandbits.api.multistate.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A special {@link IAreaAccessor} that is capable of determining its own voxelshape.
 */
public interface IAreaAccessorWithVoxelShape extends IAreaAccessor
{
    /**
     * Determines the voxelshape of the {@link IAreaAccessorWithVoxelShape} at the given position.
     *
     * @param selectablePredicateBuilder The {@link IStateEntryInfo} {@link Predicate} builder used to filter out unwanted states.
     *                                   It is highly recommended to not use lamdas here, but use actual objects which have there {@link Object#equals(Object)} and
     *                                   {@link Object#hashCode()} methods implemented properly, so that caching can be performed on the calculation.
     * @param offset The offset of the {@link IAreaAccessorWithVoxelShape} from the given position.
     * @param simplify Whether to simplify the voxelshape.
     * @return The voxelshape of the {@link IAreaAccessorWithVoxelShape} at the given position.
     */
    VoxelShape provideShape(final Function<IAreaAccessor, Predicate<IStateEntryInfo>> selectablePredicateBuilder, final BlockPos offset, final boolean simplify);
}
