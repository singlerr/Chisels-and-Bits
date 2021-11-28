package mod.chiselsandbits.multistate.mutator;

import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.ONE_THOUSANDS;

public class MutatorFactory implements IMutatorFactory
{
    private static final MutatorFactory INSTANCE = new MutatorFactory();

    private MutatorFactory()
    {
    }

    public static MutatorFactory getInstance()
    {
        return INSTANCE;
    }

    /**
     * Creates a mutator which mutates a particular block only.
     *
     * @param world The world to mutate in.
     * @param pos   The position to mutate.
     * @return The mutator.
     */
    @Override
    public IWorldAreaMutator in(final LevelAccessor world, final BlockPos pos)
    {
        return new ChiselAdaptingWorldMutator(world, pos);
    }

    /**
     * Creates a mutator which mutates a given area.
     *
     * @param world The world to mutate in.
     * @param from  The block to function as a start point.
     * @param to    The block to function as an end point.
     * @return The mutator.
     */
    @Override
    public IWorldAreaMutator covering(final LevelAccessor world, final BlockPos from, final BlockPos to)
    {
        return new WorldWrappingMutator(
          world,
          Vec3.atLowerCornerOf(from),
          Vec3.atLowerCornerOf(to).add(new Vec3(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())
                                  .multiply(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide())
                                  .subtract(ONE_THOUSANDS, ONE_THOUSANDS, ONE_THOUSANDS)
          )
        );
    }

    /**
     * Creates a mutator which mutates a given area.
     *
     * @param world The world to mutate in.
     * @param from  The start point.
     * @param to    The end point.
     * @return The mutator.
     */
    @Override
    public IWorldAreaMutator covering(final LevelAccessor world, final Vec3 from, final Vec3 to)
    {
        return new WorldWrappingMutator(
          world,
          Vec3.atLowerCornerOf(new BlockPos(from.multiply(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide()))).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()).add(ONE_THOUSANDS, ONE_THOUSANDS, ONE_THOUSANDS),
          Vec3.atLowerCornerOf(new BlockPos(to.multiply(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide()))).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()).add(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit()).subtract(ONE_THOUSANDS, ONE_THOUSANDS, ONE_THOUSANDS)
        );
    }
}
