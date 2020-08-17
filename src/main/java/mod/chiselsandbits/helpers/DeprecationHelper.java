package mod.chiselsandbits.helpers;

import mod.chiselsandbits.chiseledblock.HarvestWorld;
import mod.chiselsandbits.utils.SingleBlockBlockReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings( "deprecation" )
public class DeprecationHelper
{

	public static int getLightValue(
			final BlockState state )
	{
		return state.getBlock().getLightValue( state, new SingleBlockBlockReader(state, state.getBlock()), BlockPos.ZERO );
	}

	public static BlockState getStateFromItem(
			final ItemStack bitItemStack )
	{
		if ( bitItemStack != null && bitItemStack.getItem() instanceof BlockItem)
		{
			final BlockItem blkItem = (BlockItem) bitItemStack.getItem();
			return blkItem.getBlock().getDefaultState();
		}

		return null;
	}

	@OnlyIn(Dist.CLIENT)
	public static String translateToLocal(
			final String string )
	{
		return I18n.format( string );
	}

    @OnlyIn(Dist.CLIENT)
	public static String translateToLocal(
			final String string,
			final Object... args )
	{
		return I18n.format( string, args );

	}

	public static SoundType getSoundType(
			BlockState block )
	{
		return block.getSoundType();
	}
}
