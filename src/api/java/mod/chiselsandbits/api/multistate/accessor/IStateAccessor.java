package mod.chiselsandbits.api.multistate.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public interface IStateAccessor
{
    /**
     * Gets the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     * @return An optional potentially containing the state entry of the requested target.
     */
    Optional<IStateEntryInfo> getInAreaTarget(
      Vec3 inAreaTarget
    );

    /**
     * Gets the target state in the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     * @return An optional potentially containing the state entry of the requested target.
     */
    Optional<IStateEntryInfo> getInBlockTarget(
      BlockPos inAreaBlockPosOffset,
      Vec3 inBlockTarget
    );
}
