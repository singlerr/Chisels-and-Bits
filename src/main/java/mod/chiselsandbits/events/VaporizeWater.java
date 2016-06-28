package mod.chiselsandbits.events;

import mod.chiselsandbits.api.EventFullBlockRestoration;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VaporizeWater
{

	@SubscribeEvent
	public void handle(
			final EventFullBlockRestoration e )
	{
		if ( e.getState().getBlock() == Blocks.WATER && e.getWorld().provider.doesWaterVaporize() )
		{
			e.getWorld().setBlockToAir( e.getPos() );
			e.setCanceled( true );
		}
	}

}
