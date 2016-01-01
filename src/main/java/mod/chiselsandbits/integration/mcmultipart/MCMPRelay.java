package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.client.multipart.MultipartRegistryClient;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartRegistry;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import net.minecraft.tileentity.TileEntity;

public class MCMPRelay
{

	public final static String block_name = ChiselsAndBits.MODID + ":chisledblock";

	protected void initModIfPossible()
	{
		MultipartRegistry.registerPart( ChisledBlockPart.class, block_name );
		final ChisledBlockConverter converter = new ChisledBlockConverter();
		MultipartRegistry.registerPartConverter( converter );
		MultipartRegistry.registerReversePartConverter( converter );
		MultipartRegistryClient.bindMultipartSpecialRenderer( ChisledBlockPart.class, new ChisledBlockRenderChunkMPSR() );
	}

	protected void removePartIfPossible(
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

	protected void convertIfPossible(
			final TileEntity current,
			final TileEntityBlockChiseled newTileEntity )
	{
		if ( current instanceof IMultipartContainer )
		{
			final IMultipartContainer container = (IMultipartContainer) current;
			for ( final IMultipart part : container.getParts() )
			{
				if ( part instanceof ChisledBlockPart )
				{
					( (ChisledBlockPart) part ).swapTile( newTileEntity );
					return;
				}
			}
		}
	}

	protected TileEntityBlockChiseled getPartIfPossible(
			final TileEntity te,
			final boolean create )
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

			if ( create && !te.getWorld().isRemote )
			{
				final ChisledBlockPart part = new ChisledBlockPart();
				final TileEntityBlockChiseled tx = part.getTile();
				tx.occlusionState = new MultipartContainerBuilder( part, container );
				tx.setWorldObj( te.getWorld() );
				tx.setPos( te.getPos() );
				return tx;
			}
			else if ( create )
			{
				final ChisledBlockPart part = new ChisledBlockPart();
				part.setContainer( container );
				return part.getTile();
			}
		}

		return null;
	}

	protected boolean isMultiPart(
			final TileEntity te )
	{
		return te instanceof IMultipartContainer;
	}
}
