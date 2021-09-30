package mod.chiselsandbits.neighborhood;

import mod.chiselsandbits.api.neighborhood.IBlockNeighborhood;
import net.minecraft.util.Direction;

import java.util.EnumMap;
import java.util.Objects;

public final class BlockNeighborhood implements IBlockNeighborhood
{
    private final EnumMap<Direction, BlockNeighborhoodEntry> neighborhoodMap;

    public BlockNeighborhood(final EnumMap<Direction, BlockNeighborhoodEntry> neighborhoodMap) {this.neighborhoodMap = neighborhoodMap;}

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BlockNeighborhood))
        {
            return false;
        }

        final BlockNeighborhood that = (BlockNeighborhood) o;

        return Objects.equals(neighborhoodMap, that.neighborhoodMap);
    }

    @Override
    public int hashCode()
    {
        return neighborhoodMap != null ? neighborhoodMap.hashCode() : 0;
    }
}
