package mod.chiselsandbits.client.culling;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.core.Direction;
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

		if (a.getBlock() instanceof IFluidBlock && b.getBlock() instanceof IFluidBlock)
		{
		    final IFluidBlock aFluidBlock = (IFluidBlock) a.getBlock();
		    final IFluidBlock bFluidBlock = (IFluidBlock) b.getBlock();

            return aFluidBlock.getFluid() != bFluidBlock.getFluid();
		}

        if (a.getBlock() instanceof LiquidBlock && b.getBlock() instanceof LiquidBlock)
        {
            final LiquidBlock aFluidBlock = (LiquidBlock) a.getBlock();
            final LiquidBlock bFluidBlock = (LiquidBlock) b.getBlock();

            return aFluidBlock.getFluid() != bFluidBlock.getFluid();
        }

        if (a.getBlock() instanceof IFluidBlock && b.getBlock() instanceof LiquidBlock)
        {
            final IFluidBlock aFluidBlock = (IFluidBlock) a.getBlock();
            final LiquidBlock bFluidBlock = (LiquidBlock) b.getBlock();

            return aFluidBlock.getFluid() != bFluidBlock.getFluid();
        }

        if (a.getBlock() instanceof LiquidBlock && b.getBlock() instanceof IFluidBlock)
        {
            final LiquidBlock aFluidBlock = (LiquidBlock) a.getBlock();
            final IFluidBlock bFluidBlock = (IFluidBlock) b.getBlock();

            return aFluidBlock.getFluid() != bFluidBlock.getFluid();
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
