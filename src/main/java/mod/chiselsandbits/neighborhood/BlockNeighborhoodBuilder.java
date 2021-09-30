package mod.chiselsandbits.neighborhood;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhood;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.profiling.ProfilingManager;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

public final class BlockNeighborhoodBuilder implements IBlockNeighborhoodBuilder
{
    private static final BlockNeighborhoodBuilder INSTANCE = new BlockNeighborhoodBuilder();

    public static BlockNeighborhoodBuilder getInstance()
    {
        return INSTANCE;
    }

    private BlockNeighborhoodBuilder()
    {
    }

    @Override
    public @NotNull IBlockNeighborhood build(
      final IBlockReader reader, final BlockPos target)
    {
        final EnumMap<Direction, BlockNeighborhoodEntry> neighborhoodMap = new EnumMap<>(Direction.class);

        try (IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Key building"))
        {
            if (reader != null && target != null)
            {
                for (final Direction value : Direction.values())
                {
                    final BlockPos offsetPos = target.relative(value);
                    final BlockState state = reader.getBlockState(offsetPos);
                    final TileEntity tileEntity = reader.getBlockEntity(offsetPos);
                    if (!(tileEntity instanceof IMultiStateBlockEntity))
                    {
                        neighborhoodMap.put(value, new BlockNeighborhoodEntry(state));
                    }
                    else
                    {
                        neighborhoodMap.put(value, new BlockNeighborhoodEntry(
                            state,
                            ((IMultiStateBlockEntity) tileEntity).createNewShapeIdentifier()
                          )
                        );
                    }
                }
            }
        }

        return new BlockNeighborhood(neighborhoodMap);
    }
}
