package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.data.VoxelType;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;

/**
 * Not in use.
 * 
 * This makes separate types of materials not cull each other, but dosn't work
 * for any glasses, including FCB and vanilla.
 */
public class TransparentCullTest implements ICullTest
{

	@Override
	public boolean isVisible(
			final int mySpot,
			final int secondSpot )
	{
		final VoxelType myType = BlockBitInfo.getTypeFromStateID( mySpot );
		final VoxelType secondType = BlockBitInfo.getTypeFromStateID( secondSpot );

		final IBlockState state = ModUtil.getStateById( secondSpot );
		final boolean isTranslusent = state.getBlock().canRenderInLayer( state, BlockRenderLayer.TRANSLUCENT ) || state.getBlock().canRenderInLayer( state, BlockRenderLayer.CUTOUT )
				|| state.getBlock().canRenderInLayer( state, BlockRenderLayer.CUTOUT_MIPPED );

		return myType != VoxelType.AIR && ( myType != secondType || isTranslusent && mySpot != secondSpot );
	}

}
