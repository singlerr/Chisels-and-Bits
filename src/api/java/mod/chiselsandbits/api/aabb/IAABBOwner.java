package mod.chiselsandbits.api.aabb;

import net.minecraft.world.phys.AABB;

public interface IAABBOwner
{
    /**
     * Gives access to the bounding box of this object.
     *
     * @return The axis aligned bounding box.
     */
    AABB getBoundingBox();
}
