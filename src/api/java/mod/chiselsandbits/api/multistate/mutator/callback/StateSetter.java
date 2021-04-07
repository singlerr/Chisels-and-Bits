package mod.chiselsandbits.api.multistate.mutator.callback;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.vector.Vector3d;

@FunctionalInterface
public interface StateSetter
{

    void accept(BlockState state, Vector3d pos) throws SpaceOccupiedException;
}
