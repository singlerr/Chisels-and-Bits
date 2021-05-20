package mod.chiselsandbits.container;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.inventory.bit.IBitInventoryItem;
import mod.chiselsandbits.api.inventory.bit.IBitInventoryItemStack;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.container.helper.MergeSupportingHelperContainer;
import mod.chiselsandbits.inventory.wrapping.WrappingInventory;
import mod.chiselsandbits.network.packets.BagGuiStackPacket;
import mod.chiselsandbits.registrars.ModContainerTypes;
import mod.chiselsandbits.slots.BitSlot;
import mod.chiselsandbits.slots.ReadonlySlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BagContainer extends Container
{
    static final int OUTER_SLOT_SIZE = 18;
    final public List<Slot>      customSlots      = new ArrayList<>();
    final public List<ItemStack> customSlotsItems = new ArrayList<>();
    final PlayerEntity      thePlayer;
    final WrappingInventory visibleInventory = new WrappingInventory();

    IBitInventoryItemStack bagInv;
    ReadonlySlot           bagSlot;

    public BagContainer(final int id, final PlayerInventory playerInventory)
    {
        super(ModContainerTypes.BIT_BAG.get(), id);
        thePlayer = playerInventory.player;

        final int playerInventoryOffset = (7 - 4) * OUTER_SLOT_SIZE;

        final ItemStack is = thePlayer.getHeldItemMainhand();
        setBag(is);

        for (int yOffset = 0; yOffset < 7; ++yOffset)
        {
            for (int xOffset = 0; xOffset < 9; ++xOffset)
            {
                addCustomSlot(new BitSlot(visibleInventory, xOffset + yOffset * 9, 8 + xOffset * OUTER_SLOT_SIZE, 18 + yOffset * OUTER_SLOT_SIZE));
            }
        }

        for (int xPlayerInventory = 0; xPlayerInventory < 3; ++xPlayerInventory)
        {
            for (int yPlayerInventory = 0; yPlayerInventory < 9; ++yPlayerInventory)
            {
                addSlot(new Slot(thePlayer.inventory,
                  yPlayerInventory + xPlayerInventory * 9 + 9,
                  8 + yPlayerInventory * OUTER_SLOT_SIZE,
                  104 + xPlayerInventory * OUTER_SLOT_SIZE + playerInventoryOffset));
            }
        }

        for (int xToolbar = 0; xToolbar < 9; ++xToolbar)
        {
            if (thePlayer.inventory.currentItem == xToolbar)
            {
                addSlot(bagSlot = new ReadonlySlot(thePlayer.inventory, xToolbar, 8 + xToolbar * OUTER_SLOT_SIZE, 162 + playerInventoryOffset));
            }
            else
            {
                addSlot(new Slot(thePlayer.inventory, xToolbar, 8 + xToolbar * OUTER_SLOT_SIZE, 162 + playerInventoryOffset));
            }
        }
    }

    private void setBag(
      final ItemStack bagItem)
    {
        if (!bagItem.isEmpty() && bagItem.getItem() instanceof IBitInventoryItem)
        {
            final IBitInventoryItem bitInventoryItem = (IBitInventoryItem) bagItem.getItem();
            bagInv = bitInventoryItem.create(bagItem);
            visibleInventory.setWrapped(bagInv);
        }
        else
        {
            bagInv = null;
        }
    }

    private void addCustomSlot(
      final BitSlot newSlot)
    {
        newSlot.slotNumber = customSlots.size();
        customSlots.add(newSlot);
        customSlotsItems.add(ItemStack.EMPTY);
    }

    public void handleCustomSlotAction(
      final int slotNumber,
      final int mouseButton,
      final boolean duplicateButton,
      final boolean holdingShift)
    {
        final Slot slot = customSlots.get(slotNumber);
        final ItemStack held = thePlayer.inventory.getItemStack();
        final ItemStack slotStack = slot.getStack();

        if (duplicateButton && thePlayer.isCreative())
        {
            if (slot.getHasStack() && held.isEmpty())
            {
                final ItemStack is = slot.getStack().copy();
                is.setCount(is.getMaxStackSize());
                thePlayer.inventory.setItemStack(is);
            }
        }
        else if (holdingShift)
        {
            if (!slotStack.isEmpty())
            {
                transferStack(slotNumber, false);
            }
        }
        else if (mouseButton == 0 && !duplicateButton)
        {
            if (held.isEmpty() && slot.getHasStack())
            {
                final ItemStack pulled = slotStack.copy();
                pulled.setCount(Math.min(pulled.getMaxStackSize(), pulled.getCount()));

                final ItemStack newStackSlot = slotStack.copy();
                newStackSlot.setCount(pulled.getCount() >= slotStack.getCount() ? 0 : slotStack.getCount() - pulled.getCount());

                slot.putStack(newStackSlot.getCount() <= 0 ? ItemStack.EMPTY : newStackSlot);
                thePlayer.inventory.setItemStack(pulled);
            }
            else if (!held.isEmpty() && slot.getHasStack() && slot.isItemValid(held))
            {
                if (held.getItem() == slotStack.getItem() && held.getDamage() == slotStack.getDamage() && ItemStack.areItemStackTagsEqual(held, slotStack))
                {
                    final ItemStack newStackSlot = slotStack.copy();
                    newStackSlot.setCount(newStackSlot.getCount() + held.getCount());
                    int held_stackSize = 0;

                    if (newStackSlot.getCount() > slot.getSlotStackLimit())
                    {
                        held_stackSize = newStackSlot.getCount() - slot.getSlotStackLimit();
                        newStackSlot.setCount(newStackSlot.getCount() - held_stackSize);
                    }

                    slot.putStack(newStackSlot);
                    held.setCount(held_stackSize);
                    thePlayer.inventory.setItemStack(held);
                }
                else
                {
                    if (!held.isEmpty() && slot.getHasStack() && slotStack.getCount() <= slotStack.getMaxStackSize())
                    {
                        slot.putStack(held);
                        thePlayer.inventory.setItemStack(slotStack);
                    }
                }
            }
            else if (!held.isEmpty() && !slot.getHasStack() && slot.isItemValid(held))
            {
                slot.putStack(held);
                thePlayer.inventory.setItemStack(ItemStack.EMPTY);
            }
        }
        else if (mouseButton == 1 && !duplicateButton)
        {
            if (held.isEmpty() && slot.getHasStack())
            {
                final ItemStack pulled = slotStack.copy();
                pulled.setCount(Math.max(1, (Math.min(pulled.getMaxStackSize(), pulled.getCount()) + 1) / 2));

                final ItemStack newStackSlot = slotStack.copy();
                newStackSlot.setCount(pulled.getCount() >= slotStack.getCount() ? 0 : slotStack.getCount() - pulled.getCount());

                slot.putStack(newStackSlot.getCount() <= 0 ? ItemStack.EMPTY : newStackSlot);
                thePlayer.inventory.setItemStack(pulled);
            }
            else if (!held.isEmpty() && slot.getHasStack() && slot.isItemValid(held))
            {
                if (held.getItem() == slotStack.getItem() && held.getDamage() == slotStack.getDamage() && ItemStack.areItemStackTagsEqual(held, slotStack))
                {
                    final ItemStack newStackSlot = slotStack.copy();
                    newStackSlot.setCount(newStackSlot.getCount() + 1);
                    int held_quantity = held.getCount() - 1;

                    if (newStackSlot.getCount() > slot.getSlotStackLimit())
                    {
                        final int diff = newStackSlot.getCount() - slot.getSlotStackLimit();
                        held_quantity += diff;
                        newStackSlot.setCount(newStackSlot.getCount() - diff);
                    }

                    slot.putStack(newStackSlot);
                    held.setCount(held_quantity);
                    thePlayer.inventory.setItemStack(!held.isEmpty() ? held : ItemStack.EMPTY);
                }
            }
            else if (!held.isEmpty() && !slot.getHasStack() && slot.isItemValid(held))
            {
                final ItemStack newStackSlot = held.copy();
                newStackSlot.setCount(newStackSlot.getCount() + 1);
                held.setCount(held.getCount() - 1);

                slot.putStack(newStackSlot);
                thePlayer.inventory.setItemStack(!held.isEmpty() ? held : ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (int slotIdx = 0; slotIdx < customSlots.size(); ++slotIdx)
        {
            final ItemStack realStack = customSlots.get(slotIdx).getStack();
            ItemStack clientStack = customSlotsItems.get(slotIdx);

            if (!ItemStack.areItemStacksEqual(clientStack, realStack))
            {
                clientStack = realStack.isEmpty() ? ItemStack.EMPTY : realStack.copy();
                customSlotsItems.set(slotIdx, clientStack);

                for (final IContainerListener cl : listeners)
                {
                    if (cl instanceof ServerPlayerEntity)
                    {
                        final BagGuiStackPacket packet = new BagGuiStackPacket(slotIdx, clientStack);
                        ChiselsAndBits.getInstance().getNetworkChannel().sendToPlayer(packet, (ServerPlayerEntity) cl);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(
      final @NotNull PlayerEntity playerIn,
      final int index)
    {
        return transferStack(index, true);
    }

    @Override
    public boolean canInteractWith(
      @NotNull final PlayerEntity playerIn)
    {
        return bagInv != null && playerIn == thePlayer && hasBagInHand(thePlayer);
    }

    private boolean hasBagInHand(
      final PlayerEntity player)
    {
        if (bagInv.toItemStack() != player.getHeldItemMainhand())
        {
            setBag(player.getHeldItemMainhand());
        }

        return bagInv != null && bagInv.toItemStack().getItem() instanceof IBitInventoryItem;
    }

    private ItemStack transferStack(
      final int index,
      final boolean normalToBag)
    {
        ItemStack someReturnValue = ItemStack.EMPTY;
        boolean reverse = true;

        final MergeSupportingHelperContainer helper = new MergeSupportingHelperContainer();

        if (!normalToBag)
        {
            helper.inventorySlots.clear();
            helper.inventorySlots.addAll(customSlots);
        }
        else
        {
            helper.inventorySlots.clear();
            helper.inventorySlots.addAll(inventorySlots);
            reverse = false;
        }

        final Slot slot = helper.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            final ItemStack transferStack = slot.getStack();
            someReturnValue = transferStack.copy();

            int extraItems = 0;
            if (transferStack.getCount() > transferStack.getMaxStackSize())
            {
                extraItems = transferStack.getCount() - transferStack.getMaxStackSize();
                transferStack.setCount(transferStack.getMaxStackSize());
            }

            if (normalToBag)
            {
                helper.inventorySlots.clear();
                helper.inventorySlots.addAll(customSlots);
            }
            else
            {
                helper.inventorySlots.clear();
                helper.inventorySlots.addAll(inventorySlots);
            }

            final Item transferItem = transferStack.getItem();
            if (transferItem instanceof IBitItem)
            {
                final IBitItem bitItem = (IBitItem) transferItem;
                bitItem.onMergeOperationWithBagBeginning();
            }
            try
            {
                if (!helper.doMergeItemStack(transferStack, 0, helper.inventorySlots.size(), reverse))
                {
                    return ItemStack.EMPTY;
                }
            }
            finally
            {
                // add the extra items back on...
                transferStack.setCount(transferStack.getCount() + extraItems);
                if (transferItem instanceof IBitItem)
                {
                    final IBitItem bitItem = (IBitItem) transferItem;
                    bitItem.onMergeOperationWithBagEnding();
                }
            }

            if (transferStack.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return someReturnValue;
    }

    public void clear(
      final ItemStack stack)
    {
        if (!stack.isEmpty() && stack.getItem() instanceof IBitItem)
        {
            final IBitItem bitItem = (IBitItem) stack.getItem();
            bagInv.clear(bitItem.getBitState(stack));
        }
        else {
            bagInv.clear();
        }

        ((ServerPlayerEntity) thePlayer).sendContainerToPlayer(this);
    }

    public void sort()
    {
        bagInv.sort();
        ((ServerPlayerEntity) thePlayer).sendContainerToPlayer(this);
    }
}
