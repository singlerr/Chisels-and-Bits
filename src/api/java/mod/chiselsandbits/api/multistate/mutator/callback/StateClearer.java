package mod.chiselsandbits.api.multistate.mutator.callback;

import net.minecraft.util.math.vector.Vector3d;

@FunctionalInterface
public interface StateClearer
{

    void accept(Vector3d pos);
}
