package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.multipart.IMultipartRegistry;
import mcmultipart.api.slot.IPartSlot;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@MCMPAddon
public class MCMultipart2Addon implements IMCMPAddon
{

	private static String TE_CHISELEDPART = ChiselsAndBits.MODID + ":mod.chiselsandbits.TileEntityChiseled";

	private boolean isEnabled()
	{
		return ChiselsAndBits.getConfig().enableMCMultipart;
	}

	public MCMultipart2Addon()
	{
		ChiselsAndBits.registerWithBus( this );
	}

	@SubscribeEvent
	public void registerSlot(
			RegistryEvent.Register<IPartSlot> e )
	{
		if ( isEnabled() )
		{
			e.getRegistry().register( MultiPartSlots.BITS );
		}
	}

	@Override
	public void registerParts(
			IMultipartRegistry registry )
	{
		if ( isEnabled() )
		{
			GameRegistry.registerTileEntity( ChiseledBlockPart.class, TE_CHISELEDPART );

			MCMultipartProxy.proxyMCMultiPart.relay = new MCMultipart2Proxy();
			for ( BlockChiseled blk : ChiselsAndBits.getBlocks().getConversions().values() )
			{
				registry.registerPartWrapper( blk, new ChiseledBlockMultiPart( blk ) );
			}
		}
	}

}
