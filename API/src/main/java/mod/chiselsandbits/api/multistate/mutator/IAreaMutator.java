package mod.chiselsandbits.api.multistate.mutator;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Stream;

public interface IAreaMutator extends IAreaAccessor
{

    /**
     * Returns all entries in the current area in a mutable fashion.
     * Includes all empty areas as areas containing an air state.
     *
     * @return A stream with a mutable state entry info for each mutable section in the area.
     */
    Stream<IMutableStateEntryInfo> mutableStream();

    /**
     * Sets the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param blockState   The blockstate.
     * @param inAreaTarget The in area offset.
     * @throws SpaceOccupiedException When the space is not clear and as such the bit can not be set.
     */
    void setInAreaTarget(
      BlockState blockState,
      Vec3 inAreaTarget
    ) throws SpaceOccupiedException;

    /**
     * Sets the target state in the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param blockState           The blockstate.
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     * @throws SpaceOccupiedException When the space is not clear and as such the bit can not be set.
     */
    void setInBlockTarget(
      BlockState blockState,
      BlockPos inAreaBlockPosOffset,
      Vec3 inBlockTarget
    ) throws SpaceOccupiedException;

    /**
     * Clears the current area, using the offset from the area as well as the in area target offset.
     *
     * @param inAreaTarget The in area offset.
     */
    void clearInAreaTarget(
      Vec3 inAreaTarget
    );

    /**
     * Clears the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     */
    void clearInBlockTarget(
      BlockPos inAreaBlockPosOffset,
      Vec3 inBlockTarget
    );

    /**
     * Overrides the target state in the current area, using the offset from the area as well as the in area target offset.
     *
     * @param blockState   The blockstate.
     * @param inAreaTarget The in area offset.
     */
    default void overrideInAreaTarget(
      BlockState blockState,
      Vec3 inAreaTarget
    )
    {
        try
        {
            setInAreaTarget(Blocks.AIR.defaultBlockState(), inAreaTarget);
            setInAreaTarget(blockState, inAreaTarget);
        }
        catch (SpaceOccupiedException ignored)
        {
        }
    }

    /**
     * Overrides the target state in the current area, using the in area block position offset as well as the in block target offset to calculate the in area offset for setting.
     *
     * @param blockState           The blockstate.
     * @param inAreaBlockPosOffset The offset of blocks in the current area.
     * @param inBlockTarget        The offset in the targeted block.
     */
    default void overrideInAreaTarget(
      BlockState blockState,
      BlockPos inAreaBlockPosOffset,
      Vec3 inBlockTarget
    ) {
        try
        {
            setInBlockTarget(Blocks.AIR.defaultBlockState(), inAreaBlockPosOffset, inBlockTarget);
            setInBlockTarget(blockState, inAreaBlockPosOffset, inBlockTarget);
        }
        catch (SpaceOccupiedException ignored)
        {
        }
    }
}
