package mod.chiselsandbits.bitbag;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.helpers.NullInventory;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketBagGuiStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
	final PassThruInv pi = new PassThruInv();

	BagInventory bagInv;
	SlotReadonly thatSlot;

	final public List<Slot> customSlots = new ArrayList<Slot>();
	final public List<ItemStack> customSlotsItems = new ArrayList<ItemStack>();

	private void addCustomSlot(
			final SlotBit newSlot )
	{
		newSlot.slotNumber = customSlots.size();
		customSlots.add( newSlot );
		customSlotsItems.add( null );
	}

	public BagContainer(
			final EntityPlayer player,
			final World world,
			final int x,
			final int y,
			final int z )
	{
		thePlayer = player;

		final int i = ( 7 - 4 ) * 18;

		final ItemStack is = player.getCurrentEquippedItem();
		setBag( is );

		for ( int j = 0; j < 7; ++j )
		{
			for ( int k = 0; k < 9; ++k )
			{
				addCustomSlot( new SlotBit( pi, k + j * 9, 8 + k * 18, 18 + j * 18 ) );
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
		{
			if ( thePlayer.inventory.currentItem == j )
			{
				addSlotToContainer( thatSlot = new SlotReadonly( thePlayer.inventory, j, 8 + j * 18, 162 + i ) );
			}
			else
			{
				addSlotToContainer( new Slot( thePlayer.inventory, j, 8 + j * 18, 162 + i ) );
			}
		}
	}

	private void setBag(
			final ItemStack is )
	{
		final IInventory inv;

		if ( is != null && is.getItem() instanceof ItemBitBag )
		{
			inv = bagInv = new BagInventory( is );
		}
		else
		{
			bagInv = null;
			inv = new NullInventory( BagStorage.max_size );
		}

		pi.setInventory( inv );
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
		if ( bagInv.getItemStack() != player.getCurrentEquippedItem() )
		{
			setBag( player.getCurrentEquippedItem() );
		}

		return bagInv != null && bagInv.getItemStack().getItem() instanceof ItemBitBag;
	}

	@Override
	public ItemStack transferStackInSlot(
			final EntityPlayer playerIn,
			final int index )
	{
		return transferStack( index, true );
	}

	private ItemStack transferStack(
			final int index,
			final boolean normalToBag )
	{
		ItemStack someReturnValue = null;
		boolean reverse = true;

		final HelperContainer helper = new HelperContainer();

		if ( !normalToBag )
		{
			helper.inventorySlots = customSlots;
		}
		else
		{
			helper.inventorySlots = inventorySlots;
			reverse = false;
		}

		final Slot slot = helper.inventorySlots.get( index );

		if ( slot != null && slot.getHasStack() )
		{
			final ItemStack transferStack = slot.getStack();
			someReturnValue = transferStack.copy();

			int extraItems = 0;
			if ( transferStack.stackSize > transferStack.getMaxStackSize() )
			{
				extraItems = transferStack.stackSize - transferStack.getMaxStackSize();
				transferStack.stackSize = transferStack.getMaxStackSize();
			}

			if ( normalToBag )
			{
				helper.inventorySlots = customSlots;
				ItemChiseledBit.inventoryHack = true;
			}
			else
			{
				helper.inventorySlots = inventorySlots;
			}

			try
			{
				if ( !helper.doMergeItemStack( transferStack, 0, helper.inventorySlots.size(), reverse ) )
				{
					return null;
				}
			}
			finally
			{
				// add the extra items back on...
				transferStack.stackSize += extraItems;
				ItemChiseledBit.inventoryHack = false;
			}

			if ( transferStack.stackSize == 0 )
			{
				slot.putStack( (ItemStack) null );
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return someReturnValue;
	}

	@SideOnly( Side.CLIENT )
	public static Object getGuiClass()
	{
		return BagGui.class;
	}

	public void handleCustomSlotAction(
			final int slotNumber,
			final int mouseButton,
			final boolean duplicateButton,
			final boolean holdingShift )
	{
		final Slot slot = customSlots.get( slotNumber );
		final ItemStack held = thePlayer.inventory.getItemStack();
		final ItemStack slotStack = slot.getStack();

		if ( duplicateButton && thePlayer.capabilities.isCreativeMode )
		{
			if ( slot.getHasStack() && held == null )
			{
				final ItemStack is = slot.getStack().copy();
				is.stackSize = is.getMaxStackSize();
				thePlayer.inventory.setItemStack( is );
			}
		}
		else if ( holdingShift )
		{
			if ( slotStack != null )
			{
				transferStack( slotNumber, false );
			}
		}
		else if ( mouseButton == 0 && !duplicateButton )
		{
			if ( held == null && slot.getHasStack() )
			{
				final ItemStack pulled = slotStack.copy();
				pulled.stackSize = Math.min( pulled.getMaxStackSize(), pulled.stackSize );

				final ItemStack newStackSlot = slotStack.copy();
				newStackSlot.stackSize = pulled.stackSize >= slotStack.stackSize ? 0 : slotStack.stackSize - pulled.stackSize;

				slot.putStack( newStackSlot.stackSize <= 0 ? null : newStackSlot );
				thePlayer.inventory.setItemStack( pulled );
			}
			else if ( held != null && slot.getHasStack() && slot.isItemValid( held ) )
			{
				if ( held.getItem() == slotStack.getItem() && held.getMetadata() == slotStack.getMetadata() && ItemStack.areItemStackTagsEqual( held, slotStack ) )
				{
					final ItemStack newStackSlot = slotStack.copy();
					newStackSlot.stackSize += held.stackSize;
					held.stackSize = 0;

					if ( newStackSlot.stackSize > slot.getSlotStackLimit() )
					{
						held.stackSize = newStackSlot.stackSize - slot.getSlotStackLimit();
						newStackSlot.stackSize -= held.stackSize;
					}

					slot.putStack( newStackSlot );
					thePlayer.inventory.setItemStack( held.stackSize > 0 ? held : null );
				}
				else
				{
					if ( held != null && slot.getHasStack() && slotStack.stackSize <= slotStack.getMaxStackSize() )
					{
						slot.putStack( held );
						thePlayer.inventory.setItemStack( slotStack );
					}
				}
			}
			else if ( held != null && !slot.getHasStack() && slot.isItemValid( held ) )
			{
				slot.putStack( held );
				thePlayer.inventory.setItemStack( null );
			}
		}
		else if ( mouseButton == 1 && !duplicateButton )
		{
			if ( held == null && slot.getHasStack() )
			{
				final ItemStack pulled = slotStack.copy();
				pulled.stackSize = Math.max( 1, ( Math.min( pulled.getMaxStackSize(), pulled.stackSize ) + 1 ) / 2 );

				final ItemStack newStackSlot = slotStack.copy();
				newStackSlot.stackSize = pulled.stackSize >= slotStack.stackSize ? 0 : slotStack.stackSize - pulled.stackSize;

				slot.putStack( newStackSlot.stackSize <= 0 ? null : newStackSlot );
				thePlayer.inventory.setItemStack( pulled );
			}
			else if ( held != null && slot.getHasStack() && slot.isItemValid( held ) )
			{
				if ( held.getItem() == slotStack.getItem() && held.getMetadata() == slotStack.getMetadata() && ItemStack.areItemStackTagsEqual( held, slotStack ) )
				{
					final ItemStack newStackSlot = slotStack.copy();
					newStackSlot.stackSize += 1;
					held.stackSize--;

					if ( newStackSlot.stackSize > slot.getSlotStackLimit() )
					{
						held.stackSize = newStackSlot.stackSize - slot.getSlotStackLimit();
						newStackSlot.stackSize -= held.stackSize;
					}

					slot.putStack( newStackSlot );
					thePlayer.inventory.setItemStack( held.stackSize > 0 ? held : null );
				}
			}
			else if ( held != null && !slot.getHasStack() && slot.isItemValid( held ) )
			{
				final ItemStack newStackSlot = held.copy();
				newStackSlot.stackSize = 1;
				held.stackSize--;

				slot.putStack( newStackSlot );
				thePlayer.inventory.setItemStack( held.stackSize > 0 ? held : null );
			}
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for ( int i = 0; i < customSlots.size(); ++i )
		{
			final ItemStack realStack = customSlots.get( i ).getStack();
			ItemStack clientstack = customSlotsItems.get( i );

			if ( !ItemStack.areItemStacksEqual( clientstack, realStack ) )
			{
				clientstack = realStack == null ? null : realStack.copy();
				customSlotsItems.set( i, clientstack );

				for ( int j = 0; j < crafters.size(); ++j )
				{
					final PacketBagGuiStack pbgs = new PacketBagGuiStack();
					pbgs.is = clientstack;
					pbgs.index = i;

					NetworkRouter.instance.sendTo( pbgs, (EntityPlayerMP) crafters.get( j ) );
				}
			}
		}
	}

}
