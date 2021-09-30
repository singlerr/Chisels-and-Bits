package mod.chiselsandbits.neighborhood;

import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import net.minecraft.block.BlockState;

import java.util.Objects;

public final class BlockNeighborhoodEntry
{
    private final BlockState           blockState;
    private final IAreaShapeIdentifier identifier;

    public BlockNeighborhoodEntry(final BlockState blockState, final IAreaShapeIdentifier identifier)
    {
        this.blockState = blockState;
        this.identifier = identifier;
    }

    public BlockNeighborhoodEntry(final BlockState blockState)
    {
        this.blockState = blockState;
        this.identifier = IAreaShapeIdentifier.DUMMY;
    }

    @Override
    public int hashCode()
    {
        int result = blockState != null ? blockState.hashCode() : 0;
        result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BlockNeighborhoodEntry))
        {
            return false;
        }

        final BlockNeighborhoodEntry that = (BlockNeighborhoodEntry) o;

        if (!Objects.equals(blockState, that.blockState))
        {
            return false;
        }
        return Objects.equals(identifier, that.identifier);
    }
}
