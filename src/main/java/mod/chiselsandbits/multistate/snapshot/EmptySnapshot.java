package mod.chiselsandbits.multistate.snapshot;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.item.multistate.IStatistics;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

public class EmptySnapshot implements IMultiStateSnapshot
{
    public static final EmptySnapshot INSTANCE = new EmptySnapshot();

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
        return Identifier.INSTANCE;
    }

    /**
     * Gives access to a stream with the entry state info inside the accessors range.
     *
     * @return The stream with the inner states.
     */
    @Override
    public Stream<IStateEntryInfo> stream()
    {
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
     * Indicates if the given target is inside of the current accessor.
     *
     * @param inAreaTarget The area target to check.
     * @return True when inside, false when not.
     */
    @Override
    public boolean isInside(final Vector3d inAreaTarget)
    {
        return false;
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
        return false;
    }

    /**
     * Creates a snapshot of the current state.
     *
     * @return The snapshot.
     */
    @Override
    public IMultiStateSnapshot createSnapshot()
    {
        return this;
    }

    @Override
    public Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator)
    {
        return Stream.empty();
    }

    /**
     * Returns all entries in the current area in a mutable fashion. Includes all empty areas as areas containing an air state.
     *
     * @return A stream with a mutable state entry info for each mutable section in the area.
     */
    @Override
    public Stream<IMutableStateEntryInfo> mutableStream()
    {
        return Stream.empty();
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
     * Converts the current snapshot to a variant which is itemstack capable.
     *
     * @return The multistate itemstack which is the itemstack nbt representation of the current snapshot.
     */
    @Override
    public IMultiStateItemStack toItemStack()
    {
        return Stack.INSTANCE;
    }

    private static final class Identifier implements IAreaShapeIdentifier {
        public static final Identifier INSTANCE = new Identifier();
    }

    private static final class Stack implements IMultiStateItemStack {

        public static final Stack INSTANCE = new Stack();

        /**
         * The statistics of the itemstack.
         *
         * @return The statistics.
         */
        @Override
        public IStatistics getStatistics()
        {
            return new IStatistics() {
                @Override
                public BlockState getPrimaryState()
                {
                    return Blocks.AIR.getDefaultState();
                }

                @Override
                public CompoundNBT serializeNBT()
                {
                    return new CompoundNBT();
                }

                @Override
                public void deserializeNBT(final CompoundNBT nbt)
                {

                }
            };
        }

        /**
         * Converts this multistack itemstack data to an actual use able itemstack.
         *
         * @return The itemstack with the data of this multistate itemstack.
         */
        @Override
        public ItemStack toItemStack()
        {
            return ItemStack.EMPTY;
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
            return Identifier.INSTANCE;
        }

        /**
         * Gives access to a stream with the entry state info inside the accessors range.
         *
         * @return The stream with the inner states.
         */
        @Override
        public Stream<IStateEntryInfo> stream()
        {
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
         * Indicates if the given target is inside of the current accessor.
         *
         * @param inAreaTarget The area target to check.
         * @return True when inside, false when not.
         */
        @Override
        public boolean isInside(final Vector3d inAreaTarget)
        {
            return false;
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
            return false;
        }

        /**
         * Creates a snapshot of the current state.
         *
         * @return The snapshot.
         */
        @Override
        public IMultiStateSnapshot createSnapshot()
        {
            return EmptySnapshot.INSTANCE;
        }

        @Override
        public Stream<IStateEntryInfo> streamWithPositionMutator(final IPositionMutator positionMutator)
        {
            return Stream.empty();
        }

        /**
         * Returns all entries in the current area in a mutable fashion. Includes all empty areas as areas containing an air state.
         *
         * @return A stream with a mutable state entry info for each mutable section in the area.
         */
        @Override
        public Stream<IMutableStateEntryInfo> mutableStream()
        {
            return Stream.empty();
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
            return new CompoundNBT();
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {

        }
    }
}
