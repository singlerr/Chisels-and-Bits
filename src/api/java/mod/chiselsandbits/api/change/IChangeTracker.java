package mod.chiselsandbits.api.change;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The change tracker for tracking changes to bit blocks.
 * Currently still work in progress.
 */
public interface IChangeTracker
{

    /**
     * The instance of the tracker.
     *
     * @return The instance.
     */
    static IChangeTracker getInstance() {
        return IChiselsAndBitsAPI.getInstance().getChangeTracker();
    }

    /**
     * Invoked when a block is broken.
     * Allows the system to track initial block changes.
     *
     * @param world The world in question to track.
     * @param blockPos The position of the breaking in the world.
     * @param snapshot The resulting snapshot.
     */
    void onBlockBroken(final World world, final BlockPos blockPos, final IMultiStateSnapshot snapshot);
}
