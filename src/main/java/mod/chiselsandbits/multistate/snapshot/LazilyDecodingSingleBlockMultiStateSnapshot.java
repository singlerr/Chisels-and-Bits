package mod.chiselsandbits.multistate.snapshot;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.callback.StateClearer;
import mod.chiselsandbits.api.multistate.mutator.callback.StateSetter;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.chunk.ChunkSection;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.BITS_PER_BLOCK_SIDE;
import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.SIZE_PER_BIT;

public class LazilyDecodingSingleBlockMultiStateSnapshot implements IMultiStateSnapshot
{

    private final CompoundNBT lazyNbtCompound;
    private       boolean      loaded           = false;
    private final ChunkSection lazyChunkSection = new ChunkSection(0);

    public LazilyDecodingSingleBlockMultiStateSnapshot(final CompoundNBT lazyNbtCompound) {this.lazyNbtCompound = lazyNbtCompound;}

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
        load();
        return new Identifier(this.lazyChunkSection);
    }

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        load();

        return BlockPosStreamProvider.getForRange(BITS_PER_BLOCK_SIDE)
                 .map(blockPos -> new StateEntry(
                     lazyChunkSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                     blockPos,
                    this::setInAreaTarget, this::clearInAreaTarget)
                 );
    }

    /**
     * Gets the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     * @return An optional potentially containing the state entry of the requested target.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Optional<IStateEntryInfo> getInAreaTarget(final Vector3d inAreaTarget)
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0 ||
              inAreaTarget.getX() >= 1 ||
              inAreaTarget.getY() >= 1 ||
              inAreaTarget.getZ() >= 1) {
            throw new IllegalArgumentException("Target is not in the current area.");
        }

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE));

        load();

        final BlockState currentState = this.lazyChunkSection.getBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ()
        );

        // TODO: Replace in 1.17 with isAir()
        return currentState.isAir(new SingleBlockBlockReader(currentState), BlockPos.ZERO) ? Optional.empty() : Optional.of(new StateEntry(
          currentState,
          inAreaPos,
          this::setInAreaTarget,
          this::clearInAreaTarget)
        );
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
            throw new IllegalStateException(String.format("The given in area block pos offset is not inside the current block: %s", inAreaBlockPosOffset));
        }

        return this.getInAreaTarget(
          inBlockTarget
        );
    }

    /**
     * Indicates if the given target is inside of the current accessor.
     *
     * @param inAreaTarget The area target to check.
     * @return True when inside, false when not.
     */
    @Override
    public boolean isInside(final Vector3d inAreaTarget)
    {
        return !(inAreaTarget.getX() < 0) &&
                 !(inAreaTarget.getY() < 0) &&
                 !(inAreaTarget.getZ() < 0) &&
                 !(inAreaTarget.getX() >= 1) &&
                 !(inAreaTarget.getY() >= 1) &&
                 !(inAreaTarget.getZ() >= 1);
    }

    /**
     * Indicates if the given target (with the given block position offset) is inside of the current accessor.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     * @return True when inside, false when not.
     */
    @Override
    public boolean isInside(final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget)
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            return false;
        }

        return isInside(inBlockTarget);
    }

    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        if (!loaded) {
            final CompoundNBT copyNbtCompound = lazyNbtCompound.copy();
            return new LazilyDecodingSingleBlockMultiStateSnapshot(copyNbtCompound);
        }

        return MultiStateSnapshotUtils.createFromSection(this.lazyChunkSection);
    }

    /**
     * Returns all entries in the current area in a mutable fashion. Includes all empty areas as areas containing an air state.
     *
     * @return A stream with a mutable state entry info for each mutable section in the area.
     */
    @Override
    public Stream<IMutableStateEntryInfo> mutableStream()
    {
        load();

        return BlockPosStreamProvider.getForRange(BITS_PER_BLOCK_SIDE)
                 .map(blockPos -> new StateEntry(
                   lazyChunkSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                   blockPos,
                   this::setInAreaTarget, this::clearInAreaTarget)
                 );
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setInAreaTarget(final BlockState blockState, final Vector3d inAreaTarget) throws SpaceOccupiedException
    {
        if (inAreaTarget.getX() < 0 ||
              inAreaTarget.getY() < 0 ||
              inAreaTarget.getZ() < 0 ||
              inAreaTarget.getX() >= 1 ||
              inAreaTarget.getY() >= 1 ||
              inAreaTarget.getZ() >= 1) {
            throw new IllegalArgumentException("Target is not in the current area.");
        }

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE));

        load();

        final BlockState currentState = this.lazyChunkSection.getBlockState(inAreaPos.getX(), inAreaPos.getY(), inAreaPos.getZ());
        // TODO: 1.17 replace with normal isAir();
        if (!currentState.isAir(new SingleBlockBlockReader(currentState), BlockPos.ZERO))
            throw new SpaceOccupiedException();

        this.lazyChunkSection.setBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockState,
          true
        );
    }

    @Override
    public void setInBlockTarget(final BlockState blockState, final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget) throws SpaceOccupiedException
    {
        if (!inAreaBlockPosOffset.equals(BlockPos.ZERO))
        {
            throw new IllegalStateException(String.format("The given in area block pos offset is not inside the current block: %s", inAreaBlockPosOffset));
        }

        this.setInAreaTarget(
          blockState,
          inBlockTarget
        );
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
              inAreaTarget.getZ() < 0 ||
              inAreaTarget.getX() >= 1 ||
              inAreaTarget.getY() >= 1 ||
              inAreaTarget.getZ() >= 1) {
            throw new IllegalArgumentException("Target is not in the current area.");
        }

        final BlockPos inAreaPos = new BlockPos(inAreaTarget.mul(BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE, BITS_PER_BLOCK_SIDE));

        load();

        final BlockState blockState = Blocks.AIR.getDefaultState();

        this.lazyChunkSection.setBlockState(
          inAreaPos.getX(),
          inAreaPos.getY(),
          inAreaPos.getZ(),
          blockState,
          true
        );
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
            throw new IllegalStateException(String.format("The given in area block pos offset is not inside the current block: %s", inAreaBlockPosOffset));
        }

        this.clearInAreaTarget(
          inBlockTarget
        );
    }

    private void load()
    {
        if (this.loaded)
            return;

        ChunkSectionUtils.deserializeNBT(this.lazyChunkSection, this.lazyNbtCompound);
        this.loaded = true;
    }

    private static class StateEntry implements IMutableStateEntryInfo {

        private final BlockState blockState;
        private final Vector3d startPoint;
        private final Vector3d endPoint;
        private final StateSetter stateSetter;
        private final StateClearer stateClearer;

        private StateEntry(
          final BlockState blockState,
          final Vector3i startPoint,
          final StateSetter stateSetter,
          final StateClearer stateClearer) {
            this.blockState = blockState;
            this.startPoint = Vector3d.copy(startPoint).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT);
            this.endPoint = Vector3d.copy(startPoint).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT).add(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT);
            this.stateSetter = stateSetter;
            this.stateClearer = stateClearer;
        }

        @Override
        public BlockState getState()
        {
            return blockState;
        }

        @Override
        public Vector3d getStartPoint()
        {
            return startPoint;
        }

        @Override
        public Vector3d getEndPoint()
        {
            return endPoint;
        }

        /**
         * Sets the current entries state.
         *
         * @param blockState The new blockstate of the entry.
         */
        @Override
        public void setState(final BlockState blockState) throws SpaceOccupiedException
        {
            stateSetter.accept(blockState, getStartPoint());
        }

        /**
         * Clears the current state entries blockstate. Effectively setting the current blockstate to air.
         */
        @Override
        public void clear()
        {
            stateClearer.accept(getStartPoint());
        }
    }

    private static class Identifier implements IAreaShapeIdentifier {
        private final long[] identifyingPayload;

        private Identifier(final ChunkSection section) {
            this.identifyingPayload = Arrays.copyOf(
              section.getData().storage.getBackingLongArray(),
              section.getData().storage.getBackingLongArray().length
            );
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof Identifier))
            {
                return false;
            }
            final Identifier that = (Identifier) o;
            return Arrays.equals(identifyingPayload, that.identifyingPayload);
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(identifyingPayload);
        }
    }
}
