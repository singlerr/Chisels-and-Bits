package mod.chiselsandbits.debug;

import mod.chiselsandbits.interfaces.IItemScrollWheel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemApiDebug extends Item implements IItemScrollWheel
{

	static enum Tests
	{
		canBeChiseled( new DebugAction.canBeChiseled() ),
		createBitItem( new DebugAction.createBitItem() ),
		getBitAccess( new DebugAction.getBitAccess() ),
		setBitAccess( new DebugAction.setBitAccess() ),
		isBlockChiseled( new DebugAction.isBlockChiseled() ),
		ItemTests( new DebugAction.ItemTests() );

		final DebugAction which;

		private Tests(
				final DebugAction action )
		{
			which = action;
		}
	};

	@Override
	public String getItemStackDisplayName(
			final ItemStack stack )
	{
		return super.getItemStackDisplayName( stack ) + " - " + getAction( stack ).name();
	}

	private Tests getAction(
			final ItemStack stack )
	{
		return Tests.values()[stack.getItemDamage()];
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
		getAction( stack ).which.run( worldIn, pos, side, hitX, hitY, hitZ, playerIn );
		return true;
	}

	@Override
	public void scroll(
			final EntityPlayer player,
			final ItemStack stack,
			final int dwheel )
	{
		stack.setItemDamage( ( stack.getItemDamage() + 1 ) % Tests.values().length );
	}

}
