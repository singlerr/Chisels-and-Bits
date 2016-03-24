package mod.chiselsandbits.events;

import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * this makes the chisel into an instant "miner", this makes the breaks as fast
 * as creative, which are converted into microbreaks.
 */
public class EventBreakSpeed
{
	@SubscribeEvent
	public void breakSpeed(
			final PlayerEvent.BreakSpeed event )
	{
		if ( event.getEntityPlayer() != null )
		{
			final ItemStack is = event.getEntityPlayer().inventory.getCurrentItem();
			if ( is != null && ( is.getItem() instanceof ItemChisel || is.getItem() instanceof ItemChiseledBit ) )
			{
				event.setNewSpeed( 9999 );
			}
		}
	}

}
