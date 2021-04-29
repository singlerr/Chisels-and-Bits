package mod.chiselsandbits.api.aabb;

import net.minecraft.util.math.AxisAlignedBB;

public interface IAABBOwner
{
    /**
     * Gives access to the bounding box of this object.
     *
     * @return The axis aligned bounding box.
     */
    AxisAlignedBB getBoundingBox();
}
