package mod.chiselsandbits.debug;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemApiDebug extends Item
{

	static enum Tests
	{
		canBeChiseled( new DebugAction.canBeChiseled() ),
		createBitItem( new DebugAction.createBitItem() ),
		getBit( new DebugAction.getBit() ),
		getBitAccess( new DebugAction.getBitAccess() ),
		setBitAccess( new DebugAction.setBitAccess() ),
		isBlockChiseled( new DebugAction.isBlockChiseled() ),
		ItemTests( new DebugAction.ItemTests() ),
		getTileClass( new DebugAction.getTileClass() ),
		occusionTest( new DebugAction.occlusionTest() );

		final DebugAction which;

		private Tests(
				final DebugAction action )
		{
			which = action;
		}
	};

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
	public boolean onItemUse(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final World worldIn,
			final BlockPos pos,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		if ( playerIn.isSneaking() )
		{
			final int newDamage = getActionID( stack ) + 1;
			setActionID( stack, newDamage % Tests.values().length );
			DebugAction.Msg( playerIn, getAction( stack ).name() );
			return true;
		}

		getAction( stack ).which.run( worldIn, pos, side, hitX, hitY, hitZ, playerIn );
		return true;
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
			return stack.getTagCompound().getInteger( "id" );
		}

		return 0;
	}

}
