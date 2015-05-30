
package mod.chiselsandbits.bitbag;

import mod.chiselsandbits.helpers.NullInventory;
import mod.chiselsandbits.items.ItemBitBag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BagContainer extends Container
{
	final EntityPlayer thePlayer;
	final PassThruInv pi;

	BagInventory bagInv;
	ReadonlySlot thatSlot;

	public BagContainer(
			final EntityPlayer player,
			final World world,
			final int x,
			final int y,
			final int z )
	{
		thePlayer = player;

		final int i = ( 7 - 4 ) * 18;
		IInventory inv;

		final ItemStack is = player.getCurrentEquippedItem();
		if ( is != null && is.getItem() instanceof ItemBitBag )
		{
			inv = bagInv = new BagInventory( is );
		}
		else
		{
			bagInv = null;
			inv = new NullInventory( 63 );
		}

		pi = new PassThruInv( inv );

		for ( int j = 0; j < 7; ++j )
		{
			for ( int k = 0; k < 9; ++k )
			{
				addSlotToContainer( new BitSlot( pi, k + j * 9, 8 + k * 18, 18 + j * 18 ) );
			}
		}

		for ( int j = 0; j < 3; ++j )
		{
			for ( int k = 0; k < 9; ++k )
			{
				addSlotToContainer( new Slot( thePlayer.inventory, k + j * 9 + 9, 8 + k * 18, 104 + j * 18 + i ) );
			}
		}

		for ( int j = 0; j < 9; ++j )
			if ( thePlayer.inventory.currentItem == j )
			{
				addSlotToContainer( thatSlot = new ReadonlySlot( thePlayer.inventory, j, 8 + j * 18, 162 + i ) );
			}
			else
			{
				addSlotToContainer( new Slot( thePlayer.inventory, j, 8 + j * 18, 162 + i ) );
			}
	}

	@Override
	public boolean canInteractWith(
			final EntityPlayer playerIn )
	{
		return bagInv != null && playerIn == thePlayer && hasBagInHand( thePlayer );
	}

	private boolean hasBagInHand(
			final EntityPlayer player )
	{
		if ( bagInv.target != player.getCurrentEquippedItem() )
		{

			final ItemStack is = player.getCurrentEquippedItem();
			if ( is != null && is.getItem() instanceof ItemBitBag )
			{
				pi.setInventory( bagInv = new BagInventory( is ) );
			}
			else
			{
				bagInv = null;
				pi.setInventory( new NullInventory( 63 ) );
				return false;
			}

		}

		return bagInv.target.getItem() instanceof ItemBitBag;
	}

	@Override
	public ItemStack transferStackInSlot(
			final EntityPlayer playerIn,
			final int index )
	{
		ItemStack itemstack = null;
		final Slot slot = ( Slot ) inventorySlots.get( index );

		if ( slot != null && slot.getHasStack() )
		{
			final ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if ( index < 7 * 9 )
			{
				if ( !mergeItemStack( itemstack1, 7 * 9, inventorySlots.size(), true ) )
					return null;
			}
			else if ( !mergeItemStack( itemstack1, 0, 7 * 9, false ) )
				return null;

			if ( itemstack1.stackSize == 0 )
			{
				slot.putStack( ( ItemStack ) null );
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	@SideOnly( Side.CLIENT )
	public static Object getGuiClass()
	{
		return BagGui.class;
	}

}
