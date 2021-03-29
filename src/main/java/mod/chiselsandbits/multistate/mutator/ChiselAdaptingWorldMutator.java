package mod.chiselsandbits.multistate.mutator;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.chiseling.IConversionManager;
import mod.chiselsandbits.api.chiseling.IEligibilityManager;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.world.IInWorldStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.common.util.Constants;

import java.util.Optional;
import java.util.stream.Stream;

public class ChiselAdaptingWorldMutator implements IWorldAreaMutator
{

    private final IWorld world;
    private final BlockPos     pos;

    public ChiselAdaptingWorldMutator(final IWorld world, final BlockPos pos)
    {
        this.world = world;
        this.pos = pos;
    }

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        final TileEntity tileEntity = getWorld().getTileEntity(getPos());
        if (tileEntity instanceof IMultiStateBlockEntity)
        {
            return ((IMultiStateBlockEntity) tileEntity).stream();
        }

        final BlockState currentState = getWorld().getBlockState(getPos());
        if (IEligibilityManager.getInstance().canBeChiseled(currentState))
        {
            return Stream.of(new PreAdaptedStateEntry(
              currentState,
              getWorld(),
              getPos()
            ));
        }

        return Stream.empty();
    }

    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        final TileEntity tileEntity = getWorld().getTileEntity(getPos());
        if (tileEntity instanceof IMultiStateBlockEntity)
        {
            return ((IMultiStateBlockEntity) tileEntity).createSnapshot();
        }

        final BlockState blockState = getWorld().getBlockState(getPos());
        final ChunkSection temporarySection = new ChunkSection(0);
        for (int x = 0; x < ChiseledBlockEntity.BITS_PER_BLOCK_SIDE; x++)
        {
            for (int y = 0; y < ChiseledBlockEntity.BITS_PER_BLOCK_SIDE; y++)
            {
                for (int z = 0; z < ChiseledBlockEntity.BITS_PER_BLOCK_SIDE; z++)
                {
                    temporarySection.setBlockState(x,y,z, blockState);
                }
            }
        }

        return MultiStateSnapshotUtils.createFromSection(temporarySection);
    }

    @Override
    public IWorld getWorld()
    {
        return world;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    @Override
    public Vector3d getStartPoint()
    {
        return Vector3d.copy(pos);
    }

    @Override
    public Vector3d getEndPoint()
    {
        return Vector3d.copy(pos).add(
          15 * ChiseledBlockEntity.SIZE_PER_BIT,
          15 * ChiseledBlockEntity.SIZE_PER_BIT,
          15 * ChiseledBlockEntity.SIZE_PER_BIT
        );
    }

    @Override
    public void setInAreaTarget(final BlockState blockState, final Vector3d inAreaTarget) throws SpaceOccupiedException
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0) {
            throw new IllegalArgumentException("The chisel adapting world mutator can only mutate blocks with an in area offset greater or equal to 0. Requested was: " + inAreaTarget.toString());
        }

        if (inAreaTarget.getX() > 1 ||
              inAreaTarget.getY() > 1 ||
              inAreaTarget.getZ() > 1) {
            throw new IllegalArgumentException("The chisel adapting world mutator can only mutate blocks with an in area offset smaller then 1. Requested was: " + inAreaTarget.toString());
        }

        final TileEntity tileEntity = getWorld().getTileEntity(getPos());
        if (tileEntity instanceof IMultiStateBlockEntity)
        {
            ((IMultiStateBlockEntity) tileEntity).setInAreaTarget(blockState, inAreaTarget);
            return;
        }

        //TODO: On 1.17 update: Replace with normal isAir()
        if (!blockState.isAir(getWorld(), getPos()))
            throw new SpaceOccupiedException();

        final BlockState currentState = getWorld().getBlockState(getPos());
        final Optional<Block> optionalWithConvertedBlock = IConversionManager.getInstance().getChiseledVariantOf(currentState);
        if (optionalWithConvertedBlock.isPresent())
        {
            final Block convertedBlock = optionalWithConvertedBlock.get();
            getWorld().setBlockState(
              getPos(),
              convertedBlock.getDefaultState(),
              Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS
            );

            final TileEntity convertedTileEntity = getWorld().getTileEntity(getPos());
            if (convertedTileEntity instanceof IMultiStateBlockEntity)
            {
                ((IMultiStateBlockEntity) convertedTileEntity).setInAreaTarget(blockState, inAreaTarget);
                return;
            }

            throw new IllegalStateException("Conversion of the existing block of type: " + currentState + " into a chiseled variant failed.");
        }
    }

    @Override
    public void setInBlockTarget(final BlockState blockState, final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget) throws SpaceOccupiedException
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO)) {
            throw new IllegalArgumentException("The chisel adapting world mutator can only mutate the given single block!");
        }

        this.setInAreaTarget(blockState, inBlockTarget);
    }

    private static class PreAdaptedStateEntry implements IInWorldStateEntryInfo
    {

        private final BlockState   state;
        private final IBlockReader world;
        private final BlockPos     pos;

        private PreAdaptedStateEntry(final BlockState state, final IBlockReader world, final BlockPos pos)
        {
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        @Override
        public BlockState getState()
        {
            return state;
        }

        @Override
        public IBlockReader getWorld()
        {
            return world;
        }

        @Override
        public BlockPos getBlockPos()
        {
            return pos;
        }

        @Override
        public Vector3d getStartPoint()
        {
            return Vector3d.ZERO;
        }

        @Override
        public Vector3d getEndPoint()
        {
            return new Vector3d(
              15 * ChiseledBlockEntity.SIZE_PER_BIT,
              15 * ChiseledBlockEntity.SIZE_PER_BIT,
              15 * ChiseledBlockEntity.SIZE_PER_BIT
            );
        }
    }
}
