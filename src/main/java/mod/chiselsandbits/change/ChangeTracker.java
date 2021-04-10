package mod.chiselsandbits.change;

import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChangeTracker implements IChangeTracker
{
    private static final ChangeTracker INSTANCE = new ChangeTracker();

    public static ChangeTracker getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onBlockBroken(
      final World world, final BlockPos blockPos, final IMultiStateSnapshot snapshot)
    {
        //TODO: Implement this properly
        //Noop
    }

    private ChangeTracker()
    {
    }
}
