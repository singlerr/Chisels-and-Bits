package mod.chiselsandbits.api.util;

import mod.chiselsandbits.api.aabb.IAABBOwner;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;

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
    LevelAccessor getWorld();

    /**
     * The start point of the object in the world.
     *
     * @return The start point.
     */
    Vec3 getInWorldStartPoint();

    /**
     * The end point of the object in the world.
     *
     * @return The end point.
     */
    Vec3 getInWorldEndPoint();

    /**
     * Gives access to the in world axis aligned bounding box of the object.
     *
     * @return The axis aligned bounding box.
     */
    default AABB getInWorldBoundingBox() {
        return new AABB(getInWorldStartPoint(), getInWorldEndPoint());
    }
}
