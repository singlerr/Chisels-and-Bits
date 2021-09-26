package mod.chiselsandbits.api.multistate.mutator.callback;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface StateSetter
{

    void accept(BlockState state, Vec3 pos) throws SpaceOccupiedException;
}
