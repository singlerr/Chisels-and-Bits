package mod.chiselsandbits.item.multistate;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.world.IInWorldStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ChiseledBlockMultiStateItemStack implements IMultiStateItemStack
{

    private final ChunkSection compressedStorage;

    public ChiseledBlockMultiStateItemStack()
    {
        this.compressedStorage = new ChunkSection(0);
    }

    public ChiseledBlockMultiStateItemStack(final ChunkSection compressedStorage) {this.compressedStorage = compressedStorage;}

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
        return new ShapeIdentifier(this.compressedStorage);
    }

    /**
     * Gives access to a stream with the entry state info inside the accessors range.
     *
     * @return The stream with the inner states.
     */
    @Override
    public Stream<IStateEntryInfo> stream()
    {
        return BlockPosStreamProvider.getForRange(ChiseledBlockEntity.BITS_PER_BLOCK_SIDE)
                 .map(blockPos -> new StateEntry(
                     this.compressedStorage.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                     blockPos
                   )
                 );
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
        return Optional.empty();
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
        return Optional.empty();
    }

    /**
     * Creates a snapshot of the current state.
     *
     * @return The snapshot.
     */
    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        return null;
    }

    /**
     * Returns all entries in the current area in a mutable fashion. Includes all empty areas as areas containing an air state.
     *
     * @return A stream with a mutable state entry info for each mutable section in the area.
     */
    @Override
    public Stream<IMutableStateEntryInfo> mutableStream()
    {
        return null;
    }

    /**
     * Sets the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param blockState   The blockstate.
     * @param inAreaTarget The in area offset.
     */
    @Override
    public void setInAreaTarget(final BlockState blockState, final Vector3d inAreaTarget) throws SpaceOccupiedException
    {

    }

    /**
     * Sets the target state in the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param blockState           The blockstate.
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     */
    @Override
    public void setInBlockTarget(final BlockState blockState, final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget) throws SpaceOccupiedException
    {

    }

    /**
     * Clears the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     */
    @Override
    public void clearInAreaTarget(final Vector3d inAreaTarget)
    {

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

    }

    /**
     * Used to write the current instances data into a packet buffer.
     *
     * @param packetBuffer The packet buffer to write into.
     */
    @Override
    public void serializeInto(@NotNull final PacketBuffer packetBuffer)
    {

    }

    /**
     * Used to read the data from the packet buffer into the current instance. Potentially overriding the data that currently already exists in the instance.
     *
     * @param packetBuffer The packet buffer to read from.
     */
    @Override
    public void deserializeFrom(@NotNull final PacketBuffer packetBuffer)
    {

    }

    @Override
    public CompoundNBT serializeNBT()
    {
        return null;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {

    }

    private static final class ShapeIdentifier implements IAreaShapeIdentifier {
        private final long[] dataArray;

        private ShapeIdentifier(final ChunkSection chunkSection) {
            dataArray = Arrays.copyOf(
              chunkSection.getData().storage.getBackingLongArray(),
              chunkSection.getData().storage.getBackingLongArray().length
            );
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof ShapeIdentifier))
            {
                return false;
            }
            final ShapeIdentifier that = (ShapeIdentifier) o;
            return Arrays.equals(dataArray, that.dataArray);
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(dataArray);
        }
    }


    private static final class StateEntry implements IStateEntryInfo
    {

        private final BlockState   state;
        private final Vector3d     startPoint;
        private final Vector3d     endPoint;

        public StateEntry(final BlockState state, final Vector3i startPoint)
        {
            this(
              state,
              Vector3d.copy(startPoint).mul(ChiseledBlockEntity.SIZE_PER_BIT, ChiseledBlockEntity.SIZE_PER_BIT, ChiseledBlockEntity.SIZE_PER_BIT),
              Vector3d.copy(startPoint).mul(ChiseledBlockEntity.SIZE_PER_BIT, ChiseledBlockEntity.SIZE_PER_BIT, ChiseledBlockEntity.SIZE_PER_BIT).add(ChiseledBlockEntity.SIZE_PER_BIT, ChiseledBlockEntity.SIZE_PER_BIT, ChiseledBlockEntity.SIZE_PER_BIT)
            );
        }

        private StateEntry(
          final BlockState state,
          final Vector3d startPoint,
          final Vector3d endPoint) {
            this.state = state;
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }

        @Override
        public BlockState getState()
        {
            return state;
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
