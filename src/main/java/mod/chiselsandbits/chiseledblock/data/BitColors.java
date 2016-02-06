package mod.chiselsandbits.chiseledblock.data;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public class BitColors
{

	private static HashMap<IBlockState, Integer> bitColor = new HashMap<IBlockState, Integer>();

	public static int getColorFor(
			final IBlockState state,
			final int renderPass )
	{
		Integer out = bitColor.get( state );

		if ( out == null )
		{
			final Block blk = state.getBlock();
			final ItemStack target = new ItemStack( blk, 1, blk.damageDropped( state ) );

			if ( target.getItem() == null )
			{
				out = 0xffffff;
			}
			else
			{
				out = target.getItem().getColorFromItemStack( target, renderPass );
			}

			bitColor.put( state, out );
		}

		return out;
	}

}
