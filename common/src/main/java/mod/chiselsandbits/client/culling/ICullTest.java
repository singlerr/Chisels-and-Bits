package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface ICullTest
{
    boolean isVisible(
      IStateEntryInfo mySpot,
      BlockInformation secondPost,
      BlockPos secondBlockPos,
      Direction dir);
}
