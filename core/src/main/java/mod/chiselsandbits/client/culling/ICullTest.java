package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.blockinformation.BlockInformation;
import net.minecraft.core.Direction;

public interface ICullTest
{

	boolean isVisible(
			IStateEntryInfo mySpot,
			IBlockInformation secondSpot,
			Direction side );

}
