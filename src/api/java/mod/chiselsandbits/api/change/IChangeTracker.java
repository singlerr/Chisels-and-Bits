package mod.chiselsandbits.api.change;

import com.google.common.collect.ImmutableMap;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Deque;
import java.util.Map;

/**
 * The change tracker for tracking changes to bit blocks.
 * Currently still work in progress.
 */
public interface IChangeTracker
{
    /**
     * Invoked when a block is broken.
     * Allows the system to last block changes.
     *
     * @param blockPos The position of the breaking in the world.
     * @param snapshot The resulting snapshot.
     */
    default void onBlockBroken(final BlockPos blockPos, final IMultiStateSnapshot snapshot) {
        this.onBlocksBroken(
          ImmutableMap.of(blockPos, snapshot)
        );
    }

    /**
     * Invoked when several blocks are broken.
     * Allows the system to last block changes.
     *
     * @param snapshots The snapshots of the blocks that are being broken.
     */
    void onBlocksBroken(final Map<BlockPos, IMultiStateSnapshot> snapshots);

    /**
     * Invoked when a block is broken.
     * Allows the system to last block changes.
     *
     * @param blockPos The position of the breaking in the world.
     * @param snapshot The resulting snapshot.
     */
    default void onLastBitsRemoved(final BlockPos blockPos, final IMultiStateSnapshot snapshot) {
        this.onBlocksBroken(
          ImmutableMap.of(blockPos, snapshot)
        );
    }

    /**
     * Invoked when several blocks are broken.
     * Allows the system to last block changes.
     *
     * @param snapshots The snapshots of the blocks that are being broken.
     */
    void onLastBitsRemoved(final Map<BlockPos, IMultiStateSnapshot> snapshots);

    /**
     * Invoked when a block is placed (or basically initially chiseled) in the world.
     *
     * @param blockPos The position a new chiseled block is created on.
     * @param initialState The initial state of the block on that position.
     * @param targetSnapshot The target snapshot that resulted in that placement.
     */
    default void onBlockPlaced(final BlockPos blockPos, final BlockState initialState, final IMultiStateSnapshot targetSnapshot) {
        this.onBlocksPlaced(
          ImmutableMap.of(blockPos, initialState),
          ImmutableMap.of(blockPos, targetSnapshot)
        );
    }

    /**
     * Invoked when several blocks are placed (or basically initially chiseled) in the world.
     *
     * @param initialStates The initial states of the blocks and their positions in the current world.
     * @param targetStates The targeted snapshots of the blocks after the placement.
     */
    void onBlocksPlaced(final Map<BlockPos, BlockState> initialStates, final Map<BlockPos, IMultiStateSnapshot> targetStates);

    /**
     * Invoked when a block is placed (or basically initially chiseled) in the world.
     *
     * @param blockPos The position a new chiseled block is created on.
     * @param initialState The initial state of the block on that position.
     * @param targetSnapshot The target snapshot that resulted in that placement.
     */
    default void onFirstBitsPlaced(final BlockPos blockPos, final BlockState initialState, final IMultiStateSnapshot targetSnapshot) {
        this.onBlocksPlaced(
          ImmutableMap.of(blockPos, initialState),
          ImmutableMap.of(blockPos, targetSnapshot)
        );
    }

    /**
     * Invoked when several blocks are placed (or basically initially chiseled) in the world.
     *
     * @param initialStates The initial states of the blocks and their positions in the current world.
     * @param targetStates The targeted snapshots of the blocks after the placement.
     */
    void onFirstBitsPlaced(final Map<BlockPos, BlockState> initialStates, final Map<BlockPos, IMultiStateSnapshot> targetStates);

    /**
     * Invoked when a chiseled block is updated from one state to the next.
     *
     * @param blockPos The position of the block updated.
     * @param before The before state.
     * @param after The after state.
     */
    default void onBlockUpdated(final BlockPos blockPos, final IMultiStateSnapshot before, final IMultiStateSnapshot after) {
        this.onBlocksUpdated(
          ImmutableMap.of(blockPos, before),
          ImmutableMap.of(blockPos, after)
        );
    }

    /**
     * Invoked when several chiseled blocks are updated from one state to the next.
     *
     * @param beforeStates The states before the update.
     * @param afterState The states after the update.
     */
    void onBlocksUpdated(Map<BlockPos, IMultiStateSnapshot> beforeStates, final Map<BlockPos, IMultiStateSnapshot> afterState);

    /**
     * Opens a subsidiary change tracker which allows for the joining of different change types together into one undo step.
     * @return The subsidiary change tracker for combining purposes.
     */
    ICombiningChangeTracker openToCombine();

    /**
     * Gets a readonly-copy of the changes in the queue.
     * @return The changes last performed and recorded by this tracker.
     */
    Deque<IChange> getChanges();
}
