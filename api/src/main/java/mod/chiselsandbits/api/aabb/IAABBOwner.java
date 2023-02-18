package mod.chiselsandbits.api.aabb;

import mod.chiselsandbits.api.multistate.StateEntrySize;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Defines objects which have an axis-aligned bounding box.
 */
public interface IAABBOwner
{
    /**
     * Gives access to the bounding box of this object.
     *
     * @return The axis aligned bounding box.
     */
    @NotNull
    AABB getBoundingBox();

    /**
     * Gives access to a bounding box that is scoped to the bit world.
     * Aka all coordinates are scaled to the bit world.
     *
     * @return The axis aligned bounding box.
     */
    @NotNull
    default AABB getBitScaledBoundingBox() {
        final AABB box = getBoundingBox();
        final Vec3 start = StateEntrySize.current().toBitPosition(new Vec3(box.minX, box.minY, box.minZ));
        final Vec3 end = StateEntrySize.current().toBitPosition(new Vec3(box.maxX, box.maxY, box.maxZ));
        return new AABB(start.scale(StateEntrySize.current().getBitsPerBlockSide()), end.scale(StateEntrySize.current().getBitsPerBlockSide()));
    }
}
