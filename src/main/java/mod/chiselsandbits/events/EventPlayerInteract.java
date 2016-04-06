package mod.chiselsandbits.events;

import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * this prevents some unwanted left click behavior..
 */
public class EventPlayerInteract
{

	@SubscribeEvent
	public void interaction(
			final LeftClickBlock event )
	{
		if ( event.getEntityPlayer() != null )
		{
			final ItemStack is = event.getItemStack();
			if ( is != null && ( is.getItem() instanceof ItemChisel || is.getItem() instanceof ItemChiseledBit ) )
			{
				event.setCanceled( true );
			}
		}
	}
}
