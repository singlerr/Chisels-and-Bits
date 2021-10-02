package mod.chiselsandbits.api.neighborhood;

import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Marker interface used to detect block neighborhoods in cache keys.
 */
public interface IBlockNeighborhood
{

    /**
     * Returns the blocks blockstate neighbor in the given direction.
     * @param direction The direction.
     * @return The blockstate
     */
    @NotNull
    BlockState getBlockState(final Direction direction);

    /**
     * Returns the blocks potential area accessor neighbor in the given direction.
     * @param direction The direction.
     * @return The area accessor.
     */
    @Nullable
    IAreaAccessor getAreaAccessor(final Direction direction);
}
