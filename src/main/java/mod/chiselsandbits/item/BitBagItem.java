package mod.chiselsandbits.item;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.inventory.bit.IBitInventoryItem;
import mod.chiselsandbits.api.inventory.bit.IBitInventoryItemStack;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.inventory.bit.IllegalBitInventory;
import mod.chiselsandbits.inventory.bit.SlottedBitInventoryItemStack;
import mod.chiselsandbits.network.packets.OpenBagGuiPacket;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.utils.SimpleInstanceCache;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class BitBagItem extends Item implements IBitInventoryItem
{

    private static final int BAG_STORAGE_SLOTS = 63;

    SimpleInstanceCache<ItemStack, List<Component>> tooltipCache = new SimpleInstanceCache<>(null, new ArrayList<>());

    public BitBagItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull Component getName(final @NotNull ItemStack stack)
    {
        DyeColor color = getDyedColor(stack);
        final Component parent = super.getName(stack);
        if (parent instanceof MutableComponent && color != null)
        {
            return ((MutableComponent) parent).append(" - ").append(new TranslatableComponent("chiselsandbits.color." + color.getName()));
        }
        else
        {
            return super.getName(stack);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final @NotNull ItemStack stack, @Nullable final Level worldIn, final @NotNull List<Component> tooltip, final @NotNull TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpBitBag, tooltip);

        if (tooltipCache.needsUpdate(stack))
        {
            final IBitInventoryItemStack inventoryItemStack = create(stack);
            tooltipCache.updateCachedValue(inventoryItemStack.listContents());
        }

        final List<Component> details = tooltipCache.getCached();
        if (details.size() <= 2 || (Minecraft.getInstance().getWindow() != null && Screen.hasShiftDown()))
        {
            tooltip.addAll(details);
        }
        else
        {
            tooltip.add(new TextComponent(LocalStrings.ShiftDetails.getLocal()));
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
      final Level worldIn,
      final Player playerIn,
      final @NotNull InteractionHand hand)
    {
        final ItemStack itemStackIn = playerIn.getItemInHand(hand);

        if (worldIn.isClientSide)
        {
            ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new OpenBagGuiPacket());
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
    }

    @Override
    public IBitInventoryItemStack create(final ItemStack stack)
    {
        if (stack.getItem() != this)
            return new SlottedBitInventoryItemStack(0, (nbt) -> ItemStack.EMPTY);

        final SlottedBitInventoryItemStack inventoryItemStack = new SlottedBitInventoryItemStack(
          BAG_STORAGE_SLOTS,
          nbt -> {
              stack.getOrCreateTag().put(NbtConstants.INVENTORY, nbt);
              return stack;
          }
        );

        if (!stack.getOrCreateTag().contains(NbtConstants.INVENTORY) && stack.getOrCreateTag().contains(NbtConstants.CONTENTS)) {
            int[] legacyContentData = stack.getOrCreateTag().getIntArray(NbtConstants.CONTENTS);
            if (legacyContentData.length != BAG_STORAGE_SLOTS * 2) {
                final int[] tmp = legacyContentData;
                legacyContentData = new int[BAG_STORAGE_SLOTS * 2];
                System.arraycopy( legacyContentData, 0, tmp, 0, Math.min( BAG_STORAGE_SLOTS * 2, tmp.length ) );
            }

            for (int i = 0; i < BAG_STORAGE_SLOTS; i++)
            {
                final int count = legacyContentData[2 * i + 1];
                final int id = legacyContentData[2 * i];

                final BlockState blockState = IBlockStateIdManager.getInstance().getBlockStateFrom(id);
                final ItemStack bitStack = IBitItemManager.getInstance().create(blockState, count);

                inventoryItemStack.setItem(i, bitStack);
            }

            stack.getOrCreateTag().remove(NbtConstants.CONTENTS);
        }

        inventoryItemStack.deserializeNBT(stack.getOrCreateTagElement(NbtConstants.INVENTORY));
        return inventoryItemStack;
    }

    @Override
    public boolean showDurabilityBar(
      final ItemStack stack)
    {
        if (!(stack.getItem() instanceof IBitInventoryItem))
            return false;

        final IBitInventoryItem item = (IBitInventoryItem) stack.getItem();
        final IBitInventoryItemStack inventoryItemStack = item.create(stack);

        return !inventoryItemStack. isEmpty();
    }

    @Override
    public double getDurabilityForDisplay(
      final ItemStack stack)
    {
        if (!(stack.getItem() instanceof IBitInventoryItem))
            return 0d;

        final IBitInventoryItem item = (IBitInventoryItem) stack.getItem();
        final IBitInventoryItemStack inventoryItemStack = item.create(stack);

        final double filledRatio = inventoryItemStack.getFilledRatio();
        return Math.min(1.0d, Math.max(0.0d, Configuration.getInstance().getClient().invertBitBagFullness.get() ? filledRatio : 1.0 - filledRatio));
    }

    @Override
    public void fillItemCategory(final @NotNull CreativeModeTab group, final @NotNull NonNullList<ItemStack> items)
    {
        if (this.allowdedIn(group))
        {
            if (this == ModItems.BIT_BAG_DEFAULT.get())
            {
                items.add(new ItemStack(this));
            }
            else
            {
                for (DyeColor color : DyeColor.values())
                {
                    items.add(dyeBag(new ItemStack(this), color));
                }
            }
        }
    }

    public static ItemStack dyeBag(
      ItemStack bag,
      DyeColor color)
    {
        ItemStack copy = bag.copy();

        if (!copy.hasTag())
        {
            copy.setTag(new CompoundTag());
        }

        if (color == null && bag.getItem() == ModItems.ITEM_BIT_BAG_DYED.get())
        {
            final ItemStack unColoredStack = new ItemStack(ModItems.BIT_BAG_DEFAULT.get());
            unColoredStack.setTag(copy.getTag());
            unColoredStack.getOrCreateTag().remove("color");
            return unColoredStack;
        }
        else if (color != null)
        {
            ItemStack coloredStack = copy;
            if (coloredStack.getItem() == ModItems.BIT_BAG_DEFAULT.get())
            {
                coloredStack = new ItemStack(ModItems.ITEM_BIT_BAG_DYED.get());
                coloredStack.setTag(copy.getTag());
            }

            coloredStack.getOrCreateTag().putString("color", color.getName());
            return coloredStack;
        }

        return copy;
    }

    public static DyeColor getDyedColor(
      ItemStack stack)
    {
        if (stack.getItem() != ModItems.ITEM_BIT_BAG_DYED.get())
        {
            return null;
        }

        if (stack.getOrCreateTag().contains("color"))
        {
            String name = stack.getOrCreateTag().getString("color");
            for (DyeColor color : DyeColor.values())
            {
                if (name.equals(color.getSerializedName()))
                {
                    return color;
                }
            }
        }

        return null;
    }

    @Override
    public boolean shouldCauseReequipAnimation(final ItemStack oldStack, final ItemStack newStack, final boolean slotChanged)
    {
        return false;
    }

}
