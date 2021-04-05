package mod.chiselsandbits.multistate.mutator;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.world.IInWorldStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.blockstate.BlockStateIdUtils;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.common.util.Constants;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ChiselAdaptingWorldMutator implements IWorldAreaMutator
{
    private final IWorld   world;
    private final BlockPos pos;

    public ChiselAdaptingWorldMutator(final IWorld world, final BlockPos pos)
    {
        this.world = world;
        this.pos = pos;
    }

    /**
     * Creates a new area shape identifier.
     * <p>
     * Note: This method always returns a new instance.
     *
     * @return The new identifier.
     */
    @Override
    public IAreaShapeIdentifier createNewShapeIdentifier()
    {
        final TileEntity tileEntity = getWorld().getTileEntity(getPos());
        if (tileEntity instanceof IMultiStateBlockEntity)
        {
            return ((IMultiStateBlockEntity) tileEntity).createNewShapeIdentifier();
        }

        return new PreAdaptedShapeIdentifier(getWorld().getBlockState(getPos()));
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

    /**
     * Gets the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     * @return An optional potentially containing the state entry of the requested target.
     */
    @Override
    public Optional<IStateEntryInfo> getInAreaTarget(final Vector3d inAreaTarget)
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0)
        {
            return Optional.empty();
        }

        if (inAreaTarget.getX() > 1 ||
              inAreaTarget.getY() > 1 ||
              inAreaTarget.getZ() > 1)
        {
            return Optional.empty();
        }

        final TileEntity tileEntity = getWorld().getTileEntity(getPos());
        if (tileEntity instanceof IMultiStateBlockEntity)
        {
            return ((IMultiStateBlockEntity) tileEntity).getInAreaTarget(inAreaTarget);
        }

        final BlockState currentState = getWorld().getBlockState(getPos());
        return Optional.of(new PreAdaptedStateEntry(currentState, getWorld(), getPos()));
    }

    /**
     * Gets the target state in the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     * @return An optional potentially containing the state entry of the requested target.
     */
    @Override
    public Optional<IStateEntryInfo> getInBlockTarget(final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget)
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            throw new IllegalArgumentException("The chisel adapting world mutator can only mutate the given single block!");
        }

        return getInAreaTarget(inBlockTarget);
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
                    temporarySection.setBlockState(x, y, z, blockState);
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

    /**
     * Returns all entries in the current area in a mutable fashion. Includes all empty areas as areas containing an air state.
     *
     * @return A stream with a mutable state entry info for each mutable section in the area.
     */
    @Override
    public Stream<IMutableStateEntryInfo> mutableStream()
    {
        final TileEntity tileEntity = getWorld().getTileEntity(getPos());
        if (tileEntity instanceof IMultiStateBlockEntity)
        {
            return ((IMultiStateBlockEntity) tileEntity).mutableStream();
        }

        final BlockState currentState = getWorld().getBlockState(getPos());
        if (IEligibilityManager.getInstance().canBeChiseled(currentState))
        {
            return BlockPosStreamProvider.getForRange(ChiseledBlockEntity.BITS_PER_BLOCK_SIDE)
                     .map(blockPos -> new MutablePreAdaptedStateEntry(
                         currentState,
                         getWorld(),
                         getPos(),
                         blockPos,
                         this::setInAreaTarget,
                         this::clearInAreaTarget
                       )
                     );
        }

        return Stream.empty();
    }

    @Override
    public void setInAreaTarget(final BlockState blockState, final Vector3d inAreaTarget) throws SpaceOccupiedException
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0)
        {
            throw new IllegalArgumentException(
              "The chisel adapting world mutator can only mutate blocks with an in area offset greater or equal to 0. Requested was: " + inAreaTarget.toString());
        }

        if (inAreaTarget.getX() > 1 ||
              inAreaTarget.getY() > 1 ||
              inAreaTarget.getZ() > 1)
        {
            throw new IllegalArgumentException(
              "The chisel adapting world mutator can only mutate blocks with an in area offset smaller then 1. Requested was: " + inAreaTarget.toString());
        }

        final TileEntity tileEntity = getWorld().getTileEntity(getPos());
        if (tileEntity instanceof IMultiStateBlockEntity)
        {
            ((IMultiStateBlockEntity) tileEntity).setInAreaTarget(blockState, inAreaTarget);
            return;
        }

        //TODO: On 1.17 update: Replace with normal isAir()
        if (!blockState.isAir(getWorld(), getPos()))
        {
            throw new SpaceOccupiedException();
        }

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
                ((IMultiStateBlockEntity) convertedTileEntity).initializeWith(currentState);
                ((IMultiStateBlockEntity) convertedTileEntity).setInAreaTarget(blockState, inAreaTarget);
                return;
            }

            throw new IllegalStateException("Conversion of the existing block of type: " + currentState + " into a chiseled variant failed.");
        }
    }

    @Override
    public void setInBlockTarget(final BlockState blockState, final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget) throws SpaceOccupiedException
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            throw new IllegalArgumentException("The chisel adapting world mutator can only mutate the given single block!");
        }

        this.setInAreaTarget(blockState, inBlockTarget);
    }

    /**
     * Clears the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     */
    @Override
    public void clearInAreaTarget(final Vector3d inAreaTarget)
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0)
        {
            throw new IllegalArgumentException(
              "The chisel adapting world mutator can only mutate blocks with an in area offset greater or equal to 0. Requested was: " + inAreaTarget.toString());
        }

        if (inAreaTarget.getX() > 1 ||
              inAreaTarget.getY() > 1 ||
              inAreaTarget.getZ() > 1)
        {
            throw new IllegalArgumentException(
              "The chisel adapting world mutator can only mutate blocks with an in area offset smaller then 1. Requested was: " + inAreaTarget.toString());
        }

        final TileEntity tileEntity = getWorld().getTileEntity(getPos());
        if (tileEntity instanceof IMultiStateBlockEntity)
        {
            ((IMultiStateBlockEntity) tileEntity).clearInAreaTarget(inAreaTarget);
            return;
        }

        final BlockState currentState = getWorld().getBlockState(getPos());
        //TODO: On 1.17 update: Replace with normal isAir()
        if (!currentState.isAir(getWorld(), getPos()))
        {
            return;
        }

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
                ((IMultiStateBlockEntity) convertedTileEntity).initializeWith(currentState);
                ((IMultiStateBlockEntity) convertedTileEntity).clearInAreaTarget(inAreaTarget);
                return;
            }

            throw new IllegalStateException("Conversion of the existing block of type: " + currentState + " into a chiseled variant failed.");
        }
    }

    /**
     * Clears the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     */
    @Override
    public void clearInBlockTarget(final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget)
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            throw new IllegalArgumentException("The chisel adapting world mutator can only mutate the given single block!");
        }

        this.clearInAreaTarget(inBlockTarget);
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

    private static class MutablePreAdaptedStateEntry implements IMutableStateEntryInfo
    {

        private       BlockState   blockState;
        private final IBlockReader world;
        private final Vector3d     startPoint;
        private final Vector3d     endPoint;
        private final BlockPos     blockPos;

        private final StateSetter  setCallback;
        private final StateClearer clearCallback;

        public MutablePreAdaptedStateEntry(
          final BlockState blockState,
          final IBlockReader world,
          final BlockPos blockPos,
          final Vector3i inBlockOffset,
          final StateSetter setCallback, final StateClearer clearCallback)
        {
            this.blockState = blockState;
            this.world = world;
            this.blockPos = blockPos;
            this.startPoint = Vector3d.copy(inBlockOffset);
            this.setCallback = setCallback;
            this.clearCallback = clearCallback;
            this.endPoint = this.startPoint.add(ChiseledBlockEntity.SIZE_PER_BIT, ChiseledBlockEntity.SIZE_PER_BIT, ChiseledBlockEntity.SIZE_PER_BIT);
        }

        /**
         * The state that this entry represents.
         *
         * @return The state.
         */
        @Override
        public BlockState getState()
        {
            return blockState;
        }

        /**
         * The start (lowest on all three axi) position of the state that this entry occupies.
         *
         * @return The start position of this entry in the given block.
         */
        @Override
        public Vector3d getStartPoint()
        {
            return startPoint;
        }

        /**
         * The end (highest on all three axi) position of the state that this entry occupies.
         *
         * @return The start position of this entry in the given block.
         */
        @Override
        public Vector3d getEndPoint()
        {
            return endPoint;
        }

        /**
         * The world, in the form of a block reader, that this entry info resides in.
         *
         * @return The world.
         */
        @Override
        public IBlockReader getWorld()
        {
            return world;
        }

        /**
         * The position of the block that this state entry is part of.
         *
         * @return The in world block position.
         */
        @Override
        public BlockPos getBlockPos()
        {
            return blockPos;
        }

        /**
         * Sets the current entries state.
         *
         * @param blockState The new blockstate of the entry.
         */
        @Override
        public void setState(final BlockState blockState) throws SpaceOccupiedException
        {
            setCallback.accept(blockState, getStartPoint());
        }

        /**
         * Clears the current state entries blockstate. Effectively setting the current blockstate to air.
         */
        @Override
        public void clear()
        {
            clearCallback.accept(getStartPoint());
        }
    }

    @FunctionalInterface
    public interface StateSetter
    {

        void accept(BlockState state, Vector3d pos) throws SpaceOccupiedException;
    }

    @FunctionalInterface
    public interface StateClearer
    {

        void accept(Vector3d pos);
    }

    private static class PreAdaptedShapeIdentifier implements IAreaShapeIdentifier
    {
        private final int blockState;

        private PreAdaptedShapeIdentifier(final BlockState blockState) {this.blockState = BlockStateIdUtils.getStateId(blockState);}

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof PreAdaptedShapeIdentifier))
            {
                return false;
            }
            final PreAdaptedShapeIdentifier that = (PreAdaptedShapeIdentifier) o;
            return blockState == that.blockState;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(blockState);
        }
    }
}
