package mod.chiselsandbits.api.neighborhood;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents a system which can build unique block neighborhoods.
 */
public interface IBlockNeighborhoodBuilder
{

    /**
     * Gives access to the current builder instance.
     * Short circuit method for the central API call.
     *
     * @return The current instance.
     */
    @NotNull
    static IBlockNeighborhoodBuilder getInstance() {
        return IChiselsAndBitsAPI.getInstance().getBlockNeighborhoodBuilder();
    }

    /**
     * Builds a block neighborhood for the requested target.
     *
     * @param neighborhoodBlockStateProvider The blockstate provider to pull the neighborhood data from.
     * @param neighborhoodAreaAccessorProvider The area accessor provider to pull the neighborhood data from.
     * @return A comparable an unique element targeting the requested position and containing the neighborhood data of the target position.
     */
    @NotNull
    IBlockNeighborhood build(
            @Nullable Function<Direction, BlockState> neighborhoodBlockStateProvider,
            @Nullable Function<Direction, IAreaAccessor> neighborhoodAreaAccessorProvider
    );
}
