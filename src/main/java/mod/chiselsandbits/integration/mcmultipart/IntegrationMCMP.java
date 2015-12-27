package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.client.multipart.MultipartRegistryClient;
import mcmultipart.multipart.MultipartRegistry;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.integration.IntegrationBase;

public class IntegrationMCMP extends IntegrationBase
{

	public final static String block_name = ChiselsAndBits.MODID + ":chisledblock";

	@Override
	public void preinit()
	{
		MultipartRegistry.registerPart( ChisledBlockPart.class, block_name );
		MultipartRegistryClient.bindMultipartSpecialRenderer( ChisledBlockPart.class, new ChisledBlockRenderChunkMPSR() );
	}

	@Override
	public void init()
	{

	}

	@Override
	public void postinit()
	{

	}

}
