package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.multipart.IMultipartRegistry;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraftforge.fml.common.registry.GameRegistry;

@MCMPAddon
public class MCMultipart2Addon implements IMCMPAddon
{

	private static String TE_CHISELEDPART = ChiselsAndBits.MODID + ":mod.chiselsandbits.TileEntityChiseled";

	@Override
	public void registerParts(
			IMultipartRegistry registry )
	{
		GameRegistry.register( MultiPartSlots.BITS );
		GameRegistry.registerTileEntity( ChiseledBlockPart.class, TE_CHISELEDPART );

		MCMultipartProxy.proxyMCMultiPart.relay = new MCMultipart2Proxy();
		for ( BlockChiseled blk : ChiselsAndBits.getBlocks().getConversions().values() )
		{
			registry.registerPartWrapper( blk, new ChiseledBlockMultiPart( blk ) );
		}
	}

}
