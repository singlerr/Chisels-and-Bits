package mod.chiselsandbits.bitbag;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.helpers.NullInventory;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketBagGuiStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BagContainer extends Container
{
	static final int OUTER_SLOT_SIZE = 18;

	final EntityPlayer thePlayer;
	final TargetedInventory visibleInventory = new TargetedInventory();

	BagInventory bagInv;
	SlotReadonly bagSlot;

	final public List<Slot> customSlots = new ArrayList<Slot>();
	final public List<ItemStack> customSlotsItems = new ArrayList<ItemStack>();

	private void addCustomSlot(
			final SlotBit newSlot )
	{
		newSlot.slotNumber = customSlots.size();
		customSlots.add( newSlot );
		customSlotsItems.add( ModUtil.getEmptyStack() );
	}

	public BagContainer(
			final EntityPlayer player,
			final World world,
			final int x,
			final int y,
			final int z )
	{
		thePlayer = player;

		final int playerInventoryOffset = ( 7 - 4 ) * OUTER_SLOT_SIZE;

		final ItemStack is = player.getHeldItemMainhand();
		setBag( is );

		for ( int yOffset = 0; yOffset < 7; ++yOffset )
		{
			for ( int xOffset = 0; xOffset < 9; ++xOffset )
			{
				addCustomSlot( new SlotBit( visibleInventory, xOffset + yOffset * 9, 8 + xOffset * OUTER_SLOT_SIZE, 18 + yOffset * OUTER_SLOT_SIZE ) );
			}
		}

		for ( int xPlayerInventory = 0; xPlayerInventory < 3; ++xPlayerInventory )
		{
			for ( int yPlayerInventory = 0; yPlayerInventory < 9; ++yPlayerInventory )
			{
				addSlotToContainer( new Slot( thePlayer.inventory, yPlayerInventory + xPlayerInventory * 9 + 9, 8 + yPlayerInventory * OUTER_SLOT_SIZE, 104 + xPlayerInventory * OUTER_SLOT_SIZE + playerInventoryOffset ) );
			}
		}

		for ( int xToolbar = 0; xToolbar < 9; ++xToolbar )
		{
			if ( thePlayer.inventory.currentItem == xToolbar )
			{
				addSlotToContainer( bagSlot = new SlotReadonly( thePlayer.inventory, xToolbar, 8 + xToolbar * OUTER_SLOT_SIZE, 162 + playerInventoryOffset ) );
			}
			else
			{
				addSlotToContainer( new Slot( thePlayer.inventory, xToolbar, 8 + xToolbar * OUTER_SLOT_SIZE, 162 + playerInventoryOffset ) );
			}
		}
	}

	private void setBag(
			final ItemStack bagItem )
	{
		final IInventory inv;

		if ( ModUtil.notEmpty( bagItem ) && bagItem.getItem() instanceof ItemBitBag )
		{
			inv = bagInv = new BagInventory( bagItem );
		}
		else
		{
			bagInv = null;
			inv = new NullInventory( BagStorage.BAG_STORAGE_SLOTS );
		}

		visibleInventory.setInventory( inv );
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
		if ( bagInv.getItemStack() != player.getHeldItemMainhand() )
		{
			setBag( player.getHeldItemMainhand() );
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
		ItemStack someReturnValue = ModUtil.getEmptyStack();
		boolean reverse = true;

		final TargetedTransferContainer helper = new TargetedTransferContainer();

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
			if ( ModUtil.getStackSize( transferStack ) > transferStack.getMaxStackSize() )
			{
				extraItems = ModUtil.getStackSize( transferStack ) - transferStack.getMaxStackSize();
				ModUtil.setStackSize( transferStack, transferStack.getMaxStackSize() );
			}

			if ( normalToBag )
			{
				helper.inventorySlots = customSlots;
				ItemChiseledBit.bitBagStackLimitHack = true;
			}
			else
			{
				helper.inventorySlots = inventorySlots;
			}

			try
			{
				if ( !helper.doMergeItemStack( transferStack, 0, helper.inventorySlots.size(), reverse ) )
				{
					return ModUtil.getEmptyStack();
				}
			}
			finally
			{
				// add the extra items back on...
				ModUtil.adjustStackSize( transferStack, extraItems );
				ItemChiseledBit.bitBagStackLimitHack = false;
			}

			if ( ModUtil.getStackSize( transferStack ) == 0 )
			{
				slot.putStack( ModUtil.getEmptyStack() );
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
			if ( slot.getHasStack() && ModUtil.isEmpty( held ) )
			{
				final ItemStack is = slot.getStack().copy();
				ModUtil.setStackSize( is, is.getMaxStackSize() );
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
			if ( ModUtil.isEmpty( held ) && slot.getHasStack() )
			{
				final ItemStack pulled = ModUtil.copy( slotStack );
				ModUtil.setStackSize( pulled, Math.min( pulled.getMaxStackSize(), ModUtil.getStackSize( pulled ) ) );

				final ItemStack newStackSlot = ModUtil.copy( slotStack );
				ModUtil.setStackSize( newStackSlot, ModUtil.getStackSize( pulled ) >= ModUtil.getStackSize( slotStack ) ? 0 : ModUtil.getStackSize( slotStack ) - ModUtil.getStackSize( pulled ) );

				slot.putStack( ModUtil.getStackSize( newStackSlot ) <= 0 ? ModUtil.getEmptyStack() : newStackSlot );
				thePlayer.inventory.setItemStack( pulled );
			}
			else if ( ModUtil.notEmpty( held ) && slot.getHasStack() && slot.isItemValid( held ) )
			{
				if ( held.getItem() == slotStack.getItem() && held.getMetadata() == slotStack.getMetadata() && ItemStack.areItemStackTagsEqual( held, slotStack ) )
				{
					final ItemStack newStackSlot = ModUtil.copy( slotStack );
					ModUtil.adjustStackSize( newStackSlot, ModUtil.getStackSize( held ) );
					int held_stackSize = 0;

					if ( ModUtil.getStackSize( newStackSlot ) > slot.getSlotStackLimit() )
					{
						held_stackSize = ModUtil.getStackSize( newStackSlot ) - slot.getSlotStackLimit();
						ModUtil.adjustStackSize( newStackSlot, -held_stackSize );
					}

					slot.putStack( newStackSlot );
					ModUtil.setStackSize( held, held_stackSize );
					thePlayer.inventory.setItemStack( held );
				}
				else
				{
					if ( ModUtil.notEmpty( held ) && slot.getHasStack() && ModUtil.getStackSize( slotStack ) <= slotStack.getMaxStackSize() )
					{
						slot.putStack( held );
						thePlayer.inventory.setItemStack( slotStack );
					}
				}
			}
			else if ( ModUtil.notEmpty( held ) && !slot.getHasStack() && slot.isItemValid( held ) )
			{
				slot.putStack( held );
				thePlayer.inventory.setItemStack( ModUtil.getEmptyStack() );
			}
		}
		else if ( mouseButton == 1 && !duplicateButton )
		{
			if ( ModUtil.isEmpty( held ) && slot.getHasStack() )
			{
				final ItemStack pulled = ModUtil.copy( slotStack );
				ModUtil.setStackSize( pulled, Math.max( 1, ( Math.min( pulled.getMaxStackSize(), ModUtil.getStackSize( pulled ) ) + 1 ) / 2 ) );

				final ItemStack newStackSlot = ModUtil.copy( slotStack );
				ModUtil.setStackSize( newStackSlot, ModUtil.getStackSize( pulled ) >= ModUtil.getStackSize( slotStack ) ? 0 : ModUtil.getStackSize( slotStack ) - ModUtil.getStackSize( pulled ) );

				slot.putStack( ModUtil.getStackSize( newStackSlot ) <= 0 ? ModUtil.getEmptyStack() : newStackSlot );
				thePlayer.inventory.setItemStack( pulled );
			}
			else if ( ModUtil.notEmpty( held ) && slot.getHasStack() && slot.isItemValid( held ) )
			{
				if ( held.getItem() == slotStack.getItem() && held.getMetadata() == slotStack.getMetadata() && ItemStack.areItemStackTagsEqual( held, slotStack ) )
				{
					final ItemStack newStackSlot = ModUtil.copy( slotStack );
					ModUtil.adjustStackSize( newStackSlot, 1 );
					int held_quantity = ModUtil.getStackSize( held ) - 1;

					if ( ModUtil.getStackSize( newStackSlot ) > slot.getSlotStackLimit() )
					{
						final int diff = ModUtil.getStackSize( newStackSlot ) - slot.getSlotStackLimit();
						held_quantity += diff;
						ModUtil.adjustStackSize( newStackSlot, -diff );
					}

					slot.putStack( newStackSlot );
					ModUtil.setStackSize( held, held_quantity );
					thePlayer.inventory.setItemStack( ModUtil.notEmpty( held ) ? held : ModUtil.getEmptyStack() );
				}
			}
			else if ( ModUtil.notEmpty( held ) && !slot.getHasStack() && slot.isItemValid( held ) )
			{
				final ItemStack newStackSlot = ModUtil.copy( held );
				ModUtil.setStackSize( newStackSlot, 1 );
				ModUtil.adjustStackSize( ModUtil.nonNull( held ), -1 );

				slot.putStack( newStackSlot );
				thePlayer.inventory.setItemStack( ModUtil.notEmpty( held ) ? held : ModUtil.getEmptyStack() );
			}
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for ( int slotIdx = 0; slotIdx < customSlots.size(); ++slotIdx )
		{
			final ItemStack realStack = customSlots.get( slotIdx ).getStack();
			ItemStack clientstack = customSlotsItems.get( slotIdx );

			if ( !ItemStack.areItemStacksEqual( clientstack, realStack ) )
			{
				clientstack = ModUtil.isEmpty( realStack ) ? ModUtil.getEmptyStack() : realStack.copy();
				customSlotsItems.set( slotIdx, clientstack );

				for ( int crafterIndex = 0; crafterIndex < listeners.size(); ++crafterIndex )
				{
					final IContainerListener cl = listeners.get( crafterIndex );

					if ( cl instanceof EntityPlayerMP )
					{
						final PacketBagGuiStack pbgs = new PacketBagGuiStack();
						pbgs.is = clientstack;
						pbgs.index = slotIdx;

						NetworkRouter.instance.sendTo( pbgs, (EntityPlayerMP) cl );
					}
				}
			}
		}
	}

	public void clear(
			final ItemStack stack )
	{
		if ( ModUtil.notEmpty( stack ) && stack.getItem() == ChiselsAndBits.getItems().itemBlockBit )
		{
			if ( bagInv.matches( stack, thePlayer.inventory.getItemStack() ) )
			{
				thePlayer.inventory.setItemStack( ModUtil.getEmptyStack() );
			}
		}

		bagInv.clear( stack );
		( (EntityPlayerMP) thePlayer ).sendContainerToPlayer( this );
	}

	public void sort()
	{
		bagInv.sort();
		( (EntityPlayerMP) thePlayer ).sendContainerToPlayer( this );
	}

}
