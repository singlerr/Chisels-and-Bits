package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.helpers.LocalStrings;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;

import org.lwjgl.input.Keyboard;

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

	class BagPos
	{
		public BagPos(
				final int x,
				final BagInventory bagInventory )
		{
			inv = bagInventory;
			index = x;
		}

		final public BagInventory inv;
		final public int index;
	};

	@SubscribeEvent
	public void pickupItems(
			final ItemPickupEvent event )
	{
		final EntityItem ei = event.pickedUp;
		if ( ei != null )
		{
			final ItemStack is = ei.getEntityItem();
			if ( is != null && is.getItem() instanceof ItemChiseledBit )
			{
				// time to clean up your inventory...

				final ArrayList<BagPos> bags = new ArrayList<BagPos>();
				final IInventory inv = event.player.inventory;
				for ( int x = 0; x < inv.getSizeInventory(); x++ )
				{
					final ItemStack which = inv.getStackInSlot( x );
					if ( which != null && which.getItem() instanceof ItemBitBag )
					{
						bags.add( new BagPos( x, new BagInventory( which ) ) );
					}
				}

				boolean seen = false;
				for ( int x = 0; x < inv.getSizeInventory(); x++ )
				{

					ItemStack which = inv.getStackInSlot( x );

					if ( which != null && which.getItem() == is.getItem() && ItemChiseledBit.sameBit( which, ItemChisel.getStackState( is ) ) )
					{
						if ( !seen )
						{
							seen = true;
						}
						else
						{
							for ( final BagPos i : bags )
							{
								which = i.inv.insertItem( which );
								if ( which == null )
								{
									inv.setInventorySlotContents( x, which );
									break;
								}
							}
						}
					}

				}
			}
		}
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
