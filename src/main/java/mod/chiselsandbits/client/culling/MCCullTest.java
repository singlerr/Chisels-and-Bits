package mod.chiselsandbits.client.culling;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.IFluidBlock;

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

		if ( a.getBlock() instanceof IFluidBlock || a.getBlock() instanceof FlowingFluidBlock)
		{
			return true;
		}

		if (a.isAir() && !b.isAir())
		    return false;

		if (b.isAir() && !a.isAir())
		    return true;

		try
		{
			return !a.isSideInvisible( b, Direction.NORTH );
		}
		catch ( final Throwable t )
		{
		    return false;
		}
	}
}
