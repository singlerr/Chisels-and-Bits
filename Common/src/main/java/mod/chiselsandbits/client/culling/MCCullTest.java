package mod.chiselsandbits.client.culling;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Determine Culling using Block's Native Check.
 *
 * hardcode vanilla stained glass because that looks horrible.
 */
public class MCCullTest implements ICullTest
{


	@Override
	public boolean isVisible(
			final BlockState a,
			final BlockState b )
	{
		if ( a == b )
		{
			return false;
		}

		if ( a.getBlock().getClass() == StainedGlassBlock.class && a.getBlock() == b.getBlock() )
		{
			return false;
		}

        if (a.getBlock() instanceof LiquidBlock && b.getBlock() instanceof LiquidBlock)
        {
            return a.getFluidState().getType().equals(b.getFluidState().getType());
        }

		if (a.isAir() && !b.isAir())
		    return false;

		if (b.isAir() && !a.isAir())
		    return true;

		try
		{
			return !a.skipRendering( b, Direction.NORTH );
		}
		catch ( final Throwable t )
		{
		    return false;
		}
	}
}
