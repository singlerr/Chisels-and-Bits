package mod.chiselsandbits.events;

import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Disable breaking blocks when using a chisel / bit, some items break too fast
 * for the other code to prevent which is where this comes in.
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
