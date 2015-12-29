package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.client.multipart.MultipartRegistryClient;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartRegistry;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.integration.IntegrationBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Loader;

public class IntegrationMCMP extends IntegrationBase
{

	public final static String block_name = ChiselsAndBits.MODID + ":chisledblock";
	public boolean canUseMCMP = Loader.isModLoaded( "mcmultipart" );

	private void initModIfPossible()
	{
		MultipartRegistry.registerPart( ChisledBlockPart.class, block_name );
		MultipartRegistry.registerPartConverter( new ChisledBlockConverter() );
		MultipartRegistryClient.bindMultipartSpecialRenderer( ChisledBlockPart.class, new ChisledBlockRenderChunkMPSR() );
	}

	private void removePartIfPossible(
			final TileEntity te )
	{
		if ( te instanceof IMultipartContainer && !te.getWorld().isRemote )
		{
			final IMultipartContainer container = (IMultipartContainer) te;
			for ( final IMultipart part : container.getParts() )
			{
				if ( part instanceof ChisledBlockPart )
				{
					container.removePart( part );
					return;
				}
			}
		}

	}

	private TileEntityBlockChiseled getPartIfPossible(
			final TileEntity te )
	{
		if ( te instanceof IMultipartContainer )
		{
			final IMultipartContainer container = (IMultipartContainer) te;
			for ( final IMultipart part : container.getParts() )
			{
				if ( part instanceof ChisledBlockPart )
				{
					return ( (ChisledBlockPart) part ).getTile();
				}
			}
		}

		return null;
	}

	@Override
	public void preinit()
	{
		if ( canUseMCMP )
		{
			initModIfPossible();
		}
	}

	@Override
	public void init()
	{

	}

	@Override
	public void postinit()
	{

	}

	public TileEntityBlockChiseled getChiseledTileEntity(
			final TileEntity te )
	{
		if ( canUseMCMP )
		{
			return getPartIfPossible( te );
		}

		return null;
	}

	public void removeChisledBlock(
			final TileEntity te )
	{
		if ( canUseMCMP )
		{
			removePartIfPossible( te );
		}
	}

}
