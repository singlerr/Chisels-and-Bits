package mod.chiselsandbits.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Implemented by C&B Blocks, can be used to request a material that represents
 * the largest quantity of a C&B block.
 */
public interface IMultiStateBlock
{
	IBlockState getPrimaryState(
			IBlockAccess world,
			BlockPos pos );
}
