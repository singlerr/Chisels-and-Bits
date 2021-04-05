package mod.chiselsandbits.api.change;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IChangeTracker
{

    static IChangeTracker getInstance() {
        return IChiselsAndBitsAPI.getInstance().getChangeTracker();
    }

    void onBlockBroken(final World world, final BlockPos blockPos, final IMultiStateSnapshot snapshot);
}
