package mod.chiselsandbits.multistate.snapshot;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.utils.ChunkSectionUtils;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.chunk.ChunkSection;

import java.util.stream.Stream;

import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.BITS_PER_BLOCK_SIDE;
import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.SIZE_PER_BIT;

public class LazilyDecodingSingleBlockMultiStateSnapshot implements IMultiStateSnapshot
{

    private final CompoundNBT lazyNbtCompound;
    private       boolean      loaded           = false;
    private final ChunkSection lazyChunkSection = new ChunkSection(0);

    public LazilyDecodingSingleBlockMultiStateSnapshot(final CompoundNBT lazyNbtCompound) {this.lazyNbtCompound = lazyNbtCompound;}

    @Override
    public Stream<IStateEntryInfo> stream()
    {
        load();

        return BlockPosStreamProvider.getForRange(BITS_PER_BLOCK_SIDE)
                 .map(blockPos -> new StateEntry(
                     lazyChunkSection.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                     blockPos
                   )
                 );
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

    private void load()
    {
        if (this.loaded)
            return;

        ChunkSectionUtils.deserializeNBT(this.lazyChunkSection, this.lazyNbtCompound);
        this.loaded = true;
    }

    private static class StateEntry implements IStateEntryInfo {

        private final BlockState blockState;
        private final Vector3d startPoint;
        private final Vector3d endPoint;

        private StateEntry(final BlockState blockState, final Vector3i startPoint) {
            this.blockState = blockState;
            this.startPoint = Vector3d.copy(startPoint).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT);
            this.endPoint = Vector3d.copy(startPoint).mul(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT).add(SIZE_PER_BIT, SIZE_PER_BIT, SIZE_PER_BIT);
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
    }
}
