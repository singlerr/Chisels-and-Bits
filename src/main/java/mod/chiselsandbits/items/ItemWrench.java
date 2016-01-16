package mod.chiselsandbits.items;

import java.util.List;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemWrench extends Item
{

	public ItemWrench()
	{
		setMaxStackSize( 1 );

		final long uses = ChiselsAndBits.getConfig().wrenchUses;
		setMaxDamage( ChiselsAndBits.getConfig().damageTools ? (int) Math.max( 0, Math.min( Short.MAX_VALUE, uses ) ) : 0 );
	}

	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List<String> tooltip,
			final boolean advanced )
	{
		super.addInformation( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpWrench, tooltip );
	}

	@Override
	public boolean onItemUseFirst(
			final ItemStack stack,
			final EntityPlayer player,
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		final IBlockState b = world.getBlockState( pos );
		if ( b != null && !player.isSneaking() )
		{
			if ( b.getBlock().rotateBlock( world, pos, side ) )
			{
				stack.damageItem( 1, player );
				world.notifyNeighborsOfStateChange( pos, b.getBlock() );
				player.swingItem();
				return !world.isRemote;
			}
		}
		return false;
	}

}