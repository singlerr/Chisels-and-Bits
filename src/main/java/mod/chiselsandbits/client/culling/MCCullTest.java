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

		if (a.getBlock() instanceof IFluidBlock && b.getBlock() instanceof IFluidBlock)
		{
		    final IFluidBlock aFluidBlock = (IFluidBlock) a.getBlock();
		    final IFluidBlock bFluidBlock = (IFluidBlock) b.getBlock();

            return aFluidBlock.getFluid() != bFluidBlock.getFluid();
		}

        if (a.getBlock() instanceof FlowingFluidBlock && b.getBlock() instanceof FlowingFluidBlock)
        {
            final FlowingFluidBlock aFluidBlock = (FlowingFluidBlock) a.getBlock();
            final FlowingFluidBlock bFluidBlock = (FlowingFluidBlock) b.getBlock();

            return aFluidBlock.getFluid() != bFluidBlock.getFluid();
        }

        if (a.getBlock() instanceof IFluidBlock && b.getBlock() instanceof FlowingFluidBlock)
        {
            final IFluidBlock aFluidBlock = (IFluidBlock) a.getBlock();
            final FlowingFluidBlock bFluidBlock = (FlowingFluidBlock) b.getBlock();

            return aFluidBlock.getFluid() != bFluidBlock.getFluid();
        }

        if (a.getBlock() instanceof FlowingFluidBlock && b.getBlock() instanceof IFluidBlock)
        {
            final FlowingFluidBlock aFluidBlock = (FlowingFluidBlock) a.getBlock();
            final IFluidBlock bFluidBlock = (IFluidBlock) b.getBlock();

            return aFluidBlock.getFluid() != bFluidBlock.getFluid();
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
