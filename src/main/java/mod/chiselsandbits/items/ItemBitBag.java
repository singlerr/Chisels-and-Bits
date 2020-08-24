package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import mod.chiselsandbits.bitbag.BagCapabilityProvider;
import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.bitbag.BagStorage;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.network.packets.PacketOpenBagGui;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.render.helpers.SimpleInstanceCache;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.Nullable;

public class ItemBitBag extends Item
{

	public static final int INTS_PER_BIT_TYPE = 2;
	public static final int OFFSET_STATE_ID = 0;
	public static final int OFFSET_QUANTITY = 1;

	SimpleInstanceCache<ItemStack, List<ITextComponent>> tooltipCache = new SimpleInstanceCache<>(null, new ArrayList<>());

	public ItemBitBag(Item.Properties properties)
	{
	    super(properties.maxStackSize(1));
        MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public ICapabilityProvider initCapabilities(
			final ItemStack stack,
			final CompoundNBT nbt )
	{
		return new BagCapabilityProvider( stack, nbt );
	}

    @Override
    public ITextComponent getDisplayName(final ItemStack stack)
    {
        DyeColor color = getDyedColor( stack );
        final ITextComponent parent = super.getDisplayName(stack);
        if ( parent instanceof IFormattableTextComponent && color != null )
            return ((IFormattableTextComponent) parent).appendString(" - ").append(new TranslationTextComponent("chiselsandbits.color." + color.getTranslationKey() ));
        else
            return super.getDisplayName( stack );
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<ITextComponent> tooltip, final ITooltipFlag flagIn)
    {
        super.addInformation( stack, worldIn, tooltip, flagIn );
        ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpBitBag, tooltip );

        if ( tooltipCache.needsUpdate( stack ) )
        {
            final BagInventory bi = new BagInventory( stack );
            tooltipCache.updateCachedValue( bi.listContents(new ArrayList<>() ) );
        }

        final List<ITextComponent> details = tooltipCache.getCached();
        if ( details.size() <= 2 || ClientSide.instance.holdingShift() )
        {
            tooltip.addAll( details );
        }
        else
        {
            tooltip.add( new StringTextComponent(LocalStrings.ShiftDetails.getLocal()) );
        }
    }

	@Override
	public ActionResult<ItemStack> onItemRightClick(
			final World worldIn,
			final PlayerEntity playerIn,
			final Hand hand )
	{
		final ItemStack itemStackIn = playerIn.getHeldItem( hand );

		if ( worldIn.isRemote )
		{
			ChiselsAndBits.getNetworkChannel().sendToServer( new PacketOpenBagGui() );
		}

		return new ActionResult<ItemStack>( ActionResultType.SUCCESS, itemStackIn );
	}

	public static class BagPos
	{
		public BagPos(
				final BagInventory bagInventory )
		{
			inv = bagInventory;
		}

		final public BagInventory inv;
	};

	@SubscribeEvent
	public void pickupItems(
			final EntityItemPickupEvent event )
	{
		boolean modified = false;

		final ItemEntity entityItem = event.getItem();
		if ( entityItem != null )
		{
			final ItemStack is = entityItem.getItem();
			final PlayerEntity player = event.getPlayer();
			if ( is != null && is.getItem() instanceof ItemChiseledBit )
			{
				final int originalSize = ModUtil.getStackSize( is );
				final IInventory inv = player.inventory;
				final List<BagPos> bags = getBags( inv );

				// has the stack?
				final boolean seen = ModUtil.containsAtLeastOneOf( inv, is );

				if ( seen )
				{
					for ( final BagPos i : bags )
					{
						if ( entityItem.isAlive() )
						{
							modified = updateEntity( player, entityItem, i.inv.insertItem( ModUtil.nonNull( entityItem.getItem() ) ), originalSize ) || modified;
						}
					}
				}
				else
				{
					if ( ModUtil.getStackSize( is ) > is.getMaxStackSize() && entityItem.isAlive() )
					{
						final ItemStack singleStack = is.copy();
						ModUtil.setStackSize( singleStack, singleStack.getMaxStackSize() );

						if ( player.inventory.addItemStackToInventory( singleStack ) == false )
						{
							ModUtil.adjustStackSize( is, -( singleStack.getMaxStackSize() - ModUtil.getStackSize( is ) ) );
						}

						modified = updateEntity( player, entityItem, is, originalSize ) || modified;
					}
					else
					{
						return;
					}

					for ( final BagPos i : bags )
					{

						if ( entityItem.isAlive() )
						{
							modified = updateEntity( player, entityItem, i.inv.insertItem( ModUtil.nonNull( entityItem.getItem() ) ), originalSize ) || modified;
						}
					}
				}
			}

			cleanupInventory( player, is );
		}

		if ( modified )
		{
			event.setCanceled( true );
		}
	}

	private boolean updateEntity(
			final PlayerEntity player,
			final ItemEntity ei,
			ItemStack is,
			final int originalSize )
	{
		if ( is == null )
		{
			ei.remove();
			return true;
		}
		else
		{
			final int changed = ModUtil.getStackSize( is ) - ModUtil.getStackSize( ei.getItem() );
			ei.setItem( is );
			return changed != 0;
		}
	}

	static public void cleanupInventory(
			final PlayerEntity player,
			final ItemStack is )
	{
		if ( is != null && is.getItem() instanceof ItemChiseledBit )
		{
			// time to clean up your inventory...
			final IInventory inv = player.inventory;
			final List<BagPos> bags = getBags( inv );

			int firstSeen = -1;
			for ( int slot = 0; slot < inv.getSizeInventory(); slot++ )
			{
				int actingSlot = slot;
				@Nonnull
				ItemStack which = ModUtil.nonNull( inv.getStackInSlot( actingSlot ) );

				if ( which != null && which.getItem() == is.getItem() && ( ItemChiseledBit.sameBit( which, ItemChiseledBit.getStackState( is ) ) ) )
				{
					if ( actingSlot == player.inventory.currentItem )
					{
						if ( firstSeen != -1 )
						{
							actingSlot = firstSeen;
						}
						else
						{
							continue;
						}
					}

					which = ModUtil.nonNull( inv.getStackInSlot( actingSlot ) );

					if ( firstSeen == -1 )
					{
						firstSeen = actingSlot;
					}
					else
					{
						for ( final BagPos i : bags )
						{
							which = i.inv.insertItem( which );
							if ( ModUtil.isEmpty( which ) )
							{
								inv.setInventorySlotContents( actingSlot, which );
								break;
							}
						}
					}
				}

			}
		}
	}

	public static List<BagPos> getBags(
			final IInventory inv )
	{
		final ArrayList<BagPos> bags = new ArrayList<BagPos>();
		for ( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			final ItemStack which = inv.getStackInSlot( x );
			if ( which != null && which.getItem() instanceof ItemBitBag )
			{
				bags.add( new BagPos( new BagInventory( which ) ) );
			}
		}
		return bags;
	}

	@Override
	public boolean showDurabilityBar(
			final ItemStack stack )
	{
		final Object o = stack.getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null );

		if ( o instanceof BagStorage )
		{
			final int qty = ( (BagStorage) o ).getSlotsUsed();
			return qty != 0;
		}

		return false;
	}

	@Override
	public double getDurabilityForDisplay(
			final ItemStack stack )
	{
		final Object o = stack.getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null );

		if ( o instanceof BagStorage )
		{
			final int qty = ( (BagStorage) o ).getSlotsUsed();

			final double value = qty / (float) BagStorage.BAG_STORAGE_SLOTS;
			return Math.min( 1.0d, Math.max( 0.0d, ChiselsAndBits.getConfig().getClient().invertBitBagFullness.get() ? value : 1.0 - value ) );
		}

		return 0;
	}

    @Override
    public void fillItemGroup(final ItemGroup group, final NonNullList<ItemStack> items)
    {
        if ( this.isInGroup( group ) )
        {
            if (this == ModItems.ITEM_BIT_BAG_DEFAULT.get())
                items.add( new ItemStack( this ) );
            else
                for ( DyeColor color : DyeColor.values() )
                    items.add( dyeBag( new ItemStack( this ), color ) );
        }
    }


	public ItemStack dyeBag(
			ItemStack bag,
			DyeColor color )
	{
		ItemStack copy = bag.copy();

		if ( !copy.hasTag() )
			copy.setTag( new CompoundNBT() );

		if ( color == null && this == ModItems.ITEM_BIT_BAG_DYED.get() )
			copy.getTag().remove( "color" );
		else if (color != null)
			copy.getTag().putString( "color", color.getTranslationKey() );

		return copy;
	}

	public DyeColor getDyedColor(
			ItemStack stack )
	{
	    if (stack.getItem() != ModItems.ITEM_BIT_BAG_DYED.get())
	        return null;

		if ( stack.getOrCreateTag().contains( "color" ) )
		{
			String name = stack.getTag().getString( "color" );
			for ( DyeColor color : DyeColor.values() )
			{
				if ( name.equals( color.getString() ) )
					return color;
			}
		}

		return null;
	}
}
