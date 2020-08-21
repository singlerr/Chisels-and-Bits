package mod.chiselsandbits.client;

import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.helpers.ModelUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemColorChisled implements IItemColor
{

	@Override
	public int getColor(
			final ItemStack stack,
			final int tint )
	{
		final BlockState state = ModUtil.getStateById( tint >> BlockColorChisled.TINT_BITS );
		final Block blk = state.getBlock();
		final Item i = Item.getItemFromBlock( blk );
		int tintValue = tint & BlockColorChisled.TINT_MASK;

		if ( i != null )
		{
			return ModelUtil.getItemStackColor( new ItemStack( i, 1), tintValue );
		}

		return 0xffffff;
	}

}
