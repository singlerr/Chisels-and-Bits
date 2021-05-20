package mod.chiselsandbits.item;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.inventory.bit.IBitInventoryItem;
import mod.chiselsandbits.api.inventory.bit.IBitInventoryItemStack;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.inventory.bit.SlottedBitInventoryItemStack;
import mod.chiselsandbits.network.packets.OpenBagGuiPacket;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.utils.SimpleInstanceCache;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BitBagItem extends Item implements IBitInventoryItem
{

    private static final int BAG_STORAGE_SLOTS = 63;

    SimpleInstanceCache<ItemStack, List<ITextComponent>> tooltipCache = new SimpleInstanceCache<>(null, new ArrayList<>());

    public BitBagItem(Properties properties)
    {
        super(properties.maxStackSize(1));
    }

    @Override
    public @NotNull ITextComponent getDisplayName(final @NotNull ItemStack stack)
    {
        DyeColor color = getDyedColor(stack);
        final ITextComponent parent = super.getDisplayName(stack);
        if (parent instanceof IFormattableTextComponent && color != null)
        {
            return ((IFormattableTextComponent) parent).appendString(" - ").append(new TranslationTextComponent("chiselsandbits.color." + color.getTranslationKey()));
        }
        else
        {
            return super.getDisplayName(stack);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(final @NotNull ItemStack stack, @Nullable final World worldIn, final @NotNull List<ITextComponent> tooltip, final @NotNull ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpBitBag, tooltip);

        if (tooltipCache.needsUpdate(stack))
        {
            final IBitInventoryItemStack inventoryItemStack = create(stack);
            tooltipCache.updateCachedValue(inventoryItemStack.listContents());
        }

        final List<ITextComponent> details = tooltipCache.getCached();
        if (details.size() <= 2 || Screen.hasShiftDown())
        {
            tooltip.addAll(details);
        }
        else
        {
            tooltip.add(new StringTextComponent(LocalStrings.ShiftDetails.getLocal()));
        }
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(
      final World worldIn,
      final PlayerEntity playerIn,
      final @NotNull Hand hand)
    {
        final ItemStack itemStackIn = playerIn.getHeldItem(hand);

        if (worldIn.isRemote)
        {
            ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new OpenBagGuiPacket());
        }

        return new ActionResult<>(ActionResultType.SUCCESS, itemStackIn);
    }

    @Override
    public IBitInventoryItemStack create(final ItemStack stack)
    {
        final SlottedBitInventoryItemStack inventoryItemStack = new SlottedBitInventoryItemStack(
          BAG_STORAGE_SLOTS,
          nbt -> {
              stack.getOrCreateTag().put(NbtConstants.INVENTORY, nbt);
              return stack;
          }
        );

        inventoryItemStack.deserializeNBT(stack.getOrCreateChildTag(NbtConstants.INVENTORY));
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
    public void fillItemGroup(final @NotNull ItemGroup group, final @NotNull NonNullList<ItemStack> items)
    {
        if (this.isInGroup(group))
        {
            if (this == ModItems.ITEM_BIT_BAG_DEFAULT.get())
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
            copy.setTag(new CompoundNBT());
        }

        if (color == null && bag.getItem() == ModItems.ITEM_BIT_BAG_DYED.get())
        {
            final ItemStack unColoredStack = new ItemStack(ModItems.ITEM_BIT_BAG_DEFAULT.get());
            unColoredStack.setTag(copy.getTag());
            unColoredStack.getOrCreateTag().remove("color");
            return unColoredStack;
        }
        else if (color != null)
        {
            ItemStack coloredStack = copy;
            if (coloredStack.getItem() == ModItems.ITEM_BIT_BAG_DEFAULT.get())
            {
                coloredStack = new ItemStack(ModItems.ITEM_BIT_BAG_DYED.get());
                coloredStack.setTag(copy.getTag());
            }

            coloredStack.getOrCreateTag().putString("color", color.getTranslationKey());
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
                if (name.equals(color.getString()))
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
