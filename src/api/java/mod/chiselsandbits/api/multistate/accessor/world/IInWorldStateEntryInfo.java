package mod.chiselsandbits.api.multistate.accessor.world;

import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.IWorldObject;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

/**
 * Represents a single state entry that actually exists in a physical world.
 *
 * @see IStateEntryInfo
 * @see net.minecraft.world.IBlockReader
 * @see IWorldAreaAccessor
 */
public interface IInWorldStateEntryInfo extends IStateEntryInfo, IWorldObject
{
    /**
     * The position of the block that this state entry is part of.
     *
     * @return The in world block position.
     */
    BlockPos getBlockPos();

    /**
     * The start (lowest on all three axi) position of the state that this entry occupies.
     *
     * @return The start position of this entry in the given world.
     */
    default Vector3d getInWorldStartPoint(){
        return Vector3d.copy(getBlockPos()).add(getStartPoint());
    }

    /**
     * The end (highest on all three axi) position of the state that this entry occupies.
     *
     * @return The end position of this entry in the given world.
     */
    default Vector3d getInWorldEndPoint(){
        return Vector3d.copy(getBlockPos()).add(getEndPoint());
    }

    /**
     * The center position of the state that this entry occupies.
     *
     * @return The center position of this entry in the given world.
     */
    default Vector3d getInWorldCenterPoint() {
        return getInWorldStartPoint().add(getInWorldEndPoint()).mul(0.5, 0.5, 0.5);
    }

    @Override
    default AxisAlignedBB getBoundingBox()
    {
        return IWorldObject.super.getBoundingBox();
    }
}
