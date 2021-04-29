package mod.chiselsandbits.multistate.mutator;

import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.*;

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
    public IWorldAreaMutator in(final IWorld world, final BlockPos pos)
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
    public IWorldAreaMutator covering(final IWorld world, final BlockPos from, final BlockPos to)
    {
        return new WorldWrappingMutator(
          world,
          Vector3d.copy(from),
          Vector3d.copy(to).add(new Vector3d(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT)
                                  .mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE)
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
    public IWorldAreaMutator covering(final IWorld world, final Vector3d from, final Vector3d to)
    {
        return new WorldWrappingMutator(
          world,
          Vector3d.copy(new BlockPos(from.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE))).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT).add(ONE_THOUSANDS, ONE_THOUSANDS, ONE_THOUSANDS),
          Vector3d.copy(new BlockPos(to.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE))).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT).add(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT).subtract(ONE_THOUSANDS, ONE_THOUSANDS, ONE_THOUSANDS)
        );
    }
}
