package mod.chiselsandbits.events;

import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * this prevents some unwanted left click behavior..
 */
public class EventPlayerInteract
{

	@SubscribeEvent
	public void interaction(
			final PlayerInteractEvent event )
	{
		if ( event.action == Action.LEFT_CLICK_BLOCK && event.entityPlayer != null )
		{
			final ItemStack is = event.entityPlayer.inventory.getCurrentItem();
			if ( is != null && ( is.getItem() instanceof ItemChisel || is.getItem() instanceof ItemChiseledBit ) )
			{
				event.setCanceled( true );
			}
		}
	}
}
