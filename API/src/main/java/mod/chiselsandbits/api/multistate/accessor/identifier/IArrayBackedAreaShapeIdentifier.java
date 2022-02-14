package mod.chiselsandbits.api.multistate.accessor.identifier;

import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Indicates that the {@link IAreaShapeIdentifier} is backed by a long array and as such its core data can directly be used to
 * compare the identifiers.
 */
public interface IArrayBackedAreaShapeIdentifier extends IAreaShapeIdentifier
{

    /**
     * Gives access to the backing long array.
     * @return The backing long array.
     */
    byte[] getBackingData();

    /**
     * The palette that is in use for this identifier.
     *
     * @return The palette list.
     */
    List<BlockState> getPalette();
}
