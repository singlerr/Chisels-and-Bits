package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;

public interface ICullTest
{

	boolean isVisible(
			IBlockInformation mySpot,
			IBlockInformation secondSpot );

}
