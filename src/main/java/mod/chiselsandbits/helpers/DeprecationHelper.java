package mod.chiselsandbits.helpers;

import mod.chiselsandbits.chiseledblock.HarvestWorld;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings( "deprecation" )
public class DeprecationHelper
{

	public static int getLightValue(
			final IBlockState state )
	{
		return state.getBlock().getLightValue( state, new HarvestWorld( state ), BlockPos.ORIGIN );
	}

	public static IBlockState getStateFromItem(
			final ItemStack bitItemStack )
	{
		if ( bitItemStack != null && bitItemStack.getItem() instanceof ItemBlock )
		{
			final ItemBlock blkItem = (ItemBlock) bitItemStack.getItem();
			return blkItem.getBlock().getStateFromMeta( blkItem.getMetadata( bitItemStack ) );
		}

		return null;
	}

	public static IBlockState getStateFromMeta(
			final Block blk,
			final int meta )
	{
		return blk.getStateFromMeta( meta );
	}

	public static String translateToLocal(
			final String string )
	{
		return I18n.translateToLocal( string );
	}

	public static String translateToLocal(
			final String string,
			final Object... args )
	{
		return I18n.translateToLocalFormatted( string, args );

	}

	public static SoundType getSoundType(
			Block block )
	{
		return block.getSoundType();
	}
}
