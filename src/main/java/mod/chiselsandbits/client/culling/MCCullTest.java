package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.IFluidBlock;

/**
 * Determine Culling using Block's Native Check.
 *
 * hardcode vanilla stained glass because that looks horrible.
 */
public class MCCullTest implements ICullTest, IBlockReader
{

	private BlockState a;
	private BlockState b;

	@Override
	public boolean isVisible(
			final int mySpot,
			final int secondSpot )
	{
		if ( mySpot == 0 || mySpot == secondSpot )
		{
			return false;
		}

		a = ModUtil.getStateById( mySpot );
		if ( a == null )
		{
			a = Blocks.AIR.getDefaultState();
		}
		b = ModUtil.getStateById( secondSpot );
		if ( b == null )
		{
			b = Blocks.AIR.getDefaultState();
		}

		if ( a.getBlock().getClass() == StainedGlassBlock.class && a.getBlock() == b.getBlock() )
		{
			return false;
		}

		if ( a.getBlock() instanceof IFluidBlock || a.getBlock() instanceof FlowingFluidBlock)
		{
			return true;
		}

		try
		{
			return !a.isSideInvisible( b, Direction.NORTH );
		}
		catch ( final Throwable t )
		{
			// revert to older logic in the event of some sort of issue.
			return BlockBitInfo.getTypeFromStateID( mySpot ).shouldShow( BlockBitInfo.getTypeFromStateID( secondSpot ) );
		}
	}

	@Override
	public TileEntity getTileEntity(
			final BlockPos pos )
	{
		return null;
	}

	@Override
	public BlockState getBlockState(
			final BlockPos pos )
	{
		return pos.equals( BlockPos.ZERO ) ? a : b;
	}

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return Fluids.EMPTY.getDefaultState();
    }
}
