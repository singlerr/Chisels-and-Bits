package mod.chiselsandbits.debug;

import mod.chiselsandbits.debug.DebugAction.Tests;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemApiDebug extends Item
{

	public ItemApiDebug()
	{
		setMaxStackSize( 1 );
		setHasSubtypes( true );
	}

	@Override
	public String getItemStackDisplayName(
			final ItemStack stack )
	{
		return super.getItemStackDisplayName( stack ) + " - " + getAction( stack ).name();
	}

	private Tests getAction(
			final ItemStack stack )
	{
		return Tests.values()[getActionID( stack )];
	}

	@Override
	public EnumActionResult onItemUse(
			final PlayerEntity playerIn,
			final World worldIn,
			final BlockPos pos,
			final Hand hand,
			final Direction side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		final ItemStack stack = playerIn.getHeldItem( hand );

		if ( playerIn.isSneaking() )
		{
			final int newDamage = getActionID( stack ) + 1;
			setActionID( stack, newDamage % Tests.values().length );
			DebugAction.Msg( playerIn, getAction( stack ).name() );
			return EnumActionResult.SUCCESS;
		}

		getAction( stack ).which.run( worldIn, pos, side, hitX, hitY, hitZ, playerIn );
		return EnumActionResult.SUCCESS;
	}

	private void setActionID(
			final ItemStack stack,
			final int i )
	{
		final NBTTagCompound o = new NBTTagCompound();
		o.setInteger( "id", i );
		stack.setTagCompound( o );
	}

	private int getActionID(
			final ItemStack stack )
	{
		if ( stack.hasTagCompound() )
		{
			return ModUtil.getTagCompound( stack ).getInteger( "id" );
		}

		return 0;
	}

}
