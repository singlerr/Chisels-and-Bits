package mod.chiselsandbits.api.change;

import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;

public interface IChangeTracker
{

    static IChangeTracker getInstance() {
        return IChiselAndBitsAPI.getInstance().getChangeTracker();
    }

    void onBlockBroken(final World world, final BlockPos blockPos, final IMultiStateSnapshot snapshot);
}
