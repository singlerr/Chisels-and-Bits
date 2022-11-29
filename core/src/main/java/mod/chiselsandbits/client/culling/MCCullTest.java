package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import net.minecraft.core.Direction;

/**
 * Determine Culling using Block's Native Checks.
 *
 * Simplified version of {@link net.minecraft.world.level.block.Block#shouldRenderFace Block.shouldRenderFace}
 */
public class MCCullTest implements ICullTest
{
	@Override
	public boolean isVisible(
			final IBlockInformation a,
			final IBlockInformation b )
	{
		if ( a == b )
		{
			return false;
		}

		try
		{
			if (a.getBlockState().skipRendering( b.getBlockState(), Direction.NORTH ))
				return false;

			return !b.getBlockState().canOcclude();
		}
		catch ( final Throwable t )
		{
		    return false;
		}
	}
}
