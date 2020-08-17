package mod.chiselsandbits.chiseledblock;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;

public class HarvestWorld implements IBlockReader
{

	BlockState state;

	public HarvestWorld(
			final BlockState state )
	{
		this.state = state;
	}

	@Override
	public TileEntity getTileEntity(
			final BlockPos pos )
	{
		return null;
	}

    @Override
    public int getLightValue(final BlockPos pos)
    {
        return 0;
    }

	@Override
	public BlockState getBlockState(
			final BlockPos pos )
	{
		return state;
	}

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return
    }

    public boolean extendedLevelsInChunkCache()
	{
		return false;
	}
}
