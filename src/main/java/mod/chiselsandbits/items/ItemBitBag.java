package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketBagGuiPacket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.oredict.OreDictionary;

public class ItemBitBag extends Item
{

	public static final int intsPerBitType = 2;
	public static final int offset_state_id = 0;
	public static final int offset_qty = 1;

	public ItemBitBag()
	{
		setHasSubtypes( true );
		setMaxStackSize( 1 );
		ChiselsAndBits.registerWithBus( this );
	}

	// add info cached info
	ItemStack cachedInfo;
	List<String> details = new ArrayList<String>();

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List tooltip,
			final boolean advanced )
	{
		super.addInformation( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpBitBag, tooltip );

		if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
		{
			if ( cachedInfo != stack )
			{
				cachedInfo = stack;
				details.clear();

				final BagInventory bi = new BagInventory( stack );
				bi.listContents( details );
			}

			tooltip.addAll( details );
		}
		else
		{
			tooltip.add( LocalStrings.ShiftDetails.getLocal() );
		}
	}

	@Override
	public ItemStack onItemRightClick(
			final ItemStack itemStackIn,
			final World worldIn,
			final EntityPlayer playerIn )
	{
		if ( worldIn.isRemote )
		{
			NetworkRouter.instance.sendToServer( new PacketBagGuiPacket() );
		}

		return itemStackIn;
	}

	@Override
	public boolean onItemUseFirst(
			final ItemStack stack,
			final EntityPlayer player,
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		onItemRightClick( stack, world, player );
		return true;
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

		final EntityItem ei = event.item;
		if ( ei != null )
		{
			final ItemStack is = ei.getEntityItem();
			if ( is != null && is.getItem() instanceof ItemChiseledBit )
			{
				final int originalSize = is.stackSize;
				final IInventory inv = event.entityPlayer.inventory;
				final List<BagPos> bags = getBags( inv );

				// has the stack?
				final boolean seen = ModUtil.containsAtLeastOneOf( inv, is );

				if ( seen )
				{
					for ( final BagPos i : bags )
					{
						if ( !ei.isDead )
						{
							modified = updateEntity( event.entityPlayer, ei, i.inv.insertItem( ei.getEntityItem() ), originalSize ) || modified;
						}
					}
				}
				else
				{
					if ( is.stackSize > is.getMaxStackSize() && !ei.isDead )
					{
						final ItemStack singleStack = is.copy();
						singleStack.stackSize = singleStack.getMaxStackSize();

						if ( event.entityPlayer.inventory.addItemStackToInventory( singleStack ) == false )
						{
							is.stackSize -= singleStack.getMaxStackSize() - is.stackSize;
						}

						modified = updateEntity( event.entityPlayer, ei, is, originalSize ) || modified;
					}
					else
					{
						return;
					}

					for ( final BagPos i : bags )
					{

						if ( !ei.isDead )
						{
							modified = updateEntity( event.entityPlayer, ei, i.inv.insertItem( ei.getEntityItem() ), originalSize ) || modified;
						}
					}
				}
			}

			cleanupInventory( event.entityPlayer, is );
		}

		if ( modified )
		{
			event.setCanceled( true );
		}
	}

	private boolean updateEntity(
			final EntityPlayer player,
			final EntityItem ei,
			ItemStack is,
			final int originalSize )
	{
		if ( is == null )
		{
			is = new ItemStack( ei.getEntityItem().getItem(), 0 );
			ei.setEntityItemStack( is );
			ei.setDead();

			net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerItemPickupEvent( player, ei );

			if ( !ei.isSilent() )
			{
				ei.worldObj.playSoundAtEntity( ei, "random.pop", 0.2F, ( ( itemRand.nextFloat() - itemRand.nextFloat() ) * 0.7F + 1.0F ) * 2.0F );
			}

			player.onItemPickup( ei, originalSize );

			return true;
		}
		else
		{
			final int changed = is.stackSize - ei.getEntityItem().stackSize;
			ei.setEntityItemStack( is );
			return changed != 0;
		}
	}

	@SubscribeEvent
	public void pickupItems(
			final ItemPickupEvent event )
	{
		final EntityItem ei = event.pickedUp;
		if ( ei != null )
		{
			cleanupInventory( event.player, ei.getEntityItem() );
		}
	}

	static public void cleanupInventory(
			final EntityPlayer player,
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
				ItemStack which = inv.getStackInSlot( actingSlot );

				if ( which != null && which.getItem() == is.getItem() && ( ItemChiseledBit.sameBit( which, ItemChisel.getStackState( is ) ) || is.getItemDamage() == OreDictionary.WILDCARD_VALUE ) )
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

					which = inv.getStackInSlot( actingSlot );

					if ( firstSeen == -1 )
					{
						firstSeen = actingSlot;
					}
					else
					{
						for ( final BagPos i : bags )
						{
							which = i.inv.insertItem( which );
							if ( which == null )
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
		if ( stack.hasTagCompound() )
		{
			final int qty = BagInventory.getSlotsUsed( stack );
			return qty != 0;
		}

		return false;
	}

	@Override
	public double getDurabilityForDisplay(
			final ItemStack stack )
	{
		final int qty = BagInventory.getSlotsUsed( stack );

		final double value = qty / (float) BagInventory.max_size;
		return Math.min( 1.0d, Math.max( 0.0d, ChiselsAndBits.getConfig().invertBitBagFullness ? value : 1.0 - value ) );
	}

}
