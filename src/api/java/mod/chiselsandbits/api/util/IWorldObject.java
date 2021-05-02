package mod.chiselsandbits.api.util;

import mod.chiselsandbits.api.aabb.IAABBOwner;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

/**
 * Represents a 3D object in world.
 */
public interface IWorldObject
{

    /**
     * The world the object is in.
     *
     * @return The world.
     */
    IWorld getWorld();

    /**
     * The start point of the object in the world.
     *
     * @return The start point.
     */
    Vector3d getInWorldStartPoint();

    /**
     * The end point of the object in the world.
     *
     * @return The end point.
     */
    Vector3d getInWorldEndPoint();

    /**
     * Gives access to the in world axis aligned bounding box of the object.
     *
     * @return The axis aligned bounding box.
     */
    default AxisAlignedBB getInWorldBoundingBox() {
        return new AxisAlignedBB(getInWorldStartPoint(), getInWorldEndPoint());
    }
}
