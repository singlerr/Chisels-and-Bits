package mod.chiselsandbits.api.multistate.mutator;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public interface IAreaMutator extends IAreaAccessor
{
    /**
     * Sets the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param blockState   The blockstate.
     * @param inAreaTarget The in area offset.
     */
    void setInAreaTarget(
      BlockState blockState,
      Vector3d inAreaTarget
    ) throws SpaceOccupiedException;

    /**
     * Sets the target state in the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param blockState           The blockstate.
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targetted block.
     */
    void setInBlockTarget(
      BlockState blockState,
      BlockPos inAreaBlockPosOffset,
      Vector3d inBlockTarget
    ) throws SpaceOccupiedException;
}
