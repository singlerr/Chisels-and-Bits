package mod.chiselsandbits.events;

import java.util.WeakHashMap;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.integration.mcmultipart.MCMultipartProxy;
import mod.chiselsandbits.integration.mods.LittleTiles;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Disable breaking blocks when using a chisel / bit, some items break too fast
 * for the other code to prevent which is where this comes in.
 *
 * This manages survival chisel actions, creative some how skips this and calls
 * onBlockStartBreak on its own, but when in creative this is called on the
 * server... which still needs to be canceled or it will break the block.
 *
 * The whole things, is very strange.
 */
public class EventPlayerInteract
{

	private static WeakHashMap<EntityPlayer, Boolean> serverSuppressEvent = new WeakHashMap<EntityPlayer, Boolean>();

	public static void setPlayerSuppressionState(
			final EntityPlayer player,
			final boolean state )
	{
		if ( state )
		{
			serverSuppressEvent.put( player, state );
		}
		else
		{
			serverSuppressEvent.remove( player );
		}
	}

	@SubscribeEvent
	public void interaction(
			final LeftClickBlock event )
	{
		if ( event.getEntityPlayer() != null && event.getUseItem() != Result.DENY )
		{
			final ItemStack is = event.getItemStack();
			final boolean validEvent = event.getPos() != null && event.getWorld() != null;
			if ( is != null && ( is.getItem() instanceof ItemChisel || is.getItem() instanceof ItemChiseledBit ) && validEvent )
			{
				final IBlockState state = event.getWorld().getBlockState( event.getPos() );
				if ( BlockBitInfo.canChisel( state ) || MCMultipartProxy.proxyMCMultiPart.isMultiPartTileEntity( event.getWorld(), event.getPos() ) || LittleTiles.isLittleTilesBlock( event.getWorld().getTileEntity( event.getPos() ) ) )
				{
					if ( event.getWorld().isRemote )
					{
						// this is called when the player is survival -
						// client side.
						is.getItem().onBlockStartBreak( is, event.getPos(), event.getEntityPlayer() );
					}

					// cancel interactions vs chiseable blocks, creative is
					// magic.
					event.setCanceled( true );
				}
			}
		}

		testInteractionSupression( event, event.getUseItem() );
	}

	@SubscribeEvent
	public void interaction(
			final RightClickBlock event )
	{
		testInteractionSupression( event, event.getUseItem() );
	}

	private void testInteractionSupression(
			final PlayerInteractEvent event,
			final Result useItem )
	{
		// client is dragging...
		if ( event.getWorld().isRemote )
		{
			if ( ClientSide.instance.getStartPos() != null )
			{
				event.setCanceled( true );
			}
		}

		// server is supressed.
		if ( !event.getWorld().isRemote && event.getEntityPlayer() != null && useItem != Result.DENY )
		{
			if ( serverSuppressEvent.containsKey( event.getEntityPlayer() ) )
			{
				event.setCanceled( true );
			}
		}
	}
}
