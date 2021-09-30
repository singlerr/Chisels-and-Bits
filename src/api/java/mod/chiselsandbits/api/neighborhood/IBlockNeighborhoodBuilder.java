package mod.chiselsandbits.api.neighborhood;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.NotNull;

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
     * @param reader The block data reader to pull the neighborhood data from.
     * @param target The target position.
     * @return A comparable an unique element targeting the requested position and containing the neighborhood data of the target position.
     */
    @NotNull
    IBlockNeighborhood build(final BlockGetter reader, final BlockPos target);
}
