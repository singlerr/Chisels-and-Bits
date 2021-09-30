package mod.chiselsandbits.neighborhood;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhood;
import mod.chiselsandbits.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.profiling.ProfilingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
      final BlockGetter reader, final BlockPos target)
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
                    final BlockEntity tileEntity = reader.getBlockEntity(offsetPos);
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
