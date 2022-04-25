package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.StainedGlassBlock;

/**
 * Determine Culling using Block's Native Check.
 *
 * hardcode vanilla stained glass because that looks horrible.
 */
public class MCCullTest implements ICullTest
{
	@Override
	public boolean isVisible(
			final BlockInformation a,
			final BlockInformation b )
	{
		if ( a == b )
		{
			return false;
		}

		if ( a.getBlockState().getBlock().getClass() == StainedGlassBlock.class && a.getBlockState().getBlock() == b.getBlockState().getBlock() )
		{
			return false;
		}

        if (a.getBlockState().getBlock() instanceof LiquidBlock && b.getBlockState().getBlock() instanceof LiquidBlock)
        {
            return a.getBlockState().getFluidState().getType().equals(b.getBlockState().getFluidState().getType()) &&
              a.getVariant().equals(b.getVariant());
        }

		if (a.isAir() && !b.isAir())
		    return false;

		if (b.isAir() && !a.isAir())
		    return true;

		try
		{
			return !a.getBlockState().skipRendering( b.getBlockState(), Direction.NORTH );
		}
		catch ( final Throwable t )
		{
		    return false;
		}
	}
}
