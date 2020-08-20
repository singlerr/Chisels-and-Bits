package mod.chiselsandbits.helpers;

import java.util.*;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.items.ItemBitBag.BagPos;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;

public class BitInventoryFeeder
{
	private final static Random itemRand = new Random();
	ArrayList<Integer> seenBits = new ArrayList<>();
	boolean hasSentMessage = false;
	final PlayerEntity player;
	final World world;

	public BitInventoryFeeder(
			final PlayerEntity p,
			final World w)
	{
		player = p;
		world = w;
	}

	public void addItem(
			final ItemEntity ei)
	{
		ItemStack is = ModUtil.nonNull( ei.getItem() );

		final List<BagPos> bags = ItemBitBag.getBags( player.inventory );

		if ( !ModUtil.containsAtLeastOneOf( player.inventory, is ) )
		{
			final ItemStack minSize = is.copy();

			if ( ModUtil.getStackSize( minSize ) > minSize.getMaxStackSize() )
			{
				ModUtil.setStackSize( minSize, minSize.getMaxStackSize() );
			}

			ModUtil.adjustStackSize( is, -ModUtil.getStackSize( minSize ) );
			player.inventory.addItemStackToInventory( minSize );
			ModUtil.adjustStackSize( is, ModUtil.getStackSize( minSize ) );
		}

		for ( final BagPos bp : bags )
		{
			is = bp.inv.insertItem( is );
		}

		if ( ModUtil.isEmpty( is ) )
			return;

		ei.setItem( is );
		EntityItemPickupEvent event = new EntityItemPickupEvent( player, ei );

		if ( MinecraftForge.EVENT_BUS.post( event ) )
		{
			// cancelled...
			spawnItem( world, ei );
		}
		else
		{
			if ( event.getResult() != Event.Result.DENY )
			{
				is = ei.getItem();

				if ( is != null && !player.inventory.addItemStackToInventory( is ) )
				{
					ei.setItem( is );
					//Never spawn the items for dropped excess items if setting is enabled.
					if ( !ChiselsAndBits.getConfig().voidExcessBits )
					{
						spawnItem( world, ei );
					}
				}
				else
				{
					if ( !ei.isSilent() )
					{
						ei.world.playSound(null, ei.getPosX(), ei.getPosY(), ei.getPosZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ( ( itemRand.nextFloat() - itemRand.nextFloat() ) * 0.7F + 1.0F ) * 2.0F );
					}
				}

				player.inventory.markDirty();

				if ( player.container != null )
				{
					player.container.detectAndSendChanges();
				}

			}
			else
				spawnItem( world, ei );
		}

		final int blk = ItemChiseledBit.getStackState( is );
		if ( ChiselsAndBits.getConfig().voidExcessBits && !seenBits.contains(blk) && !hasSentMessage )
		{
			if ( !ItemChiseledBit.hasBitSpace( player, blk ) )
			{
				player.sendMessage( new TranslationTextComponent( "mod.chiselsandbits.result.void_excess" ), Util.DUMMY_UUID );
				hasSentMessage = true;
			}
			if ( !seenBits.contains( blk ))
			{
				seenBits.add( blk );
			}
		}
	}

	private static void spawnItem(
			World world,
			ItemEntity ei )
	{
		if ( world.isRemote ) // no spawning items on the client.
			return;

		world.addEntity( ei );
	}
}
