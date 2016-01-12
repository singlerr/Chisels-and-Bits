package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.client.multipart.MultipartRegistryClient;
import mcmultipart.microblock.MicroblockRegistry;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.OcclusionHelper;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.BitCollisionIterator;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.integration.ChiselsAndBitsIntegration;
import mod.chiselsandbits.integration.IntegrationBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

@ChiselsAndBitsIntegration( "mcmultipart" )
public class MCMultiPart extends IntegrationBase implements IMCMultiPart
{

	public final static String block_name = ChiselsAndBits.MODID + ":chisledblock";

	@Override
	public void preinit()
	{
		MCMultipartProxy.proxyMCMultiPart.setRelay( this );

		MultipartRegistry.registerPart( ChisledBlockPart.class, block_name );
		MicroblockRegistry.registerMicroClass( ChisledMicroblock.instance );

		final ChisledBlockConverter converter = new ChisledBlockConverter();
		MultipartRegistry.registerPartConverter( converter );
		MultipartRegistry.registerReversePartConverter( converter );

		if ( FMLCommonHandler.instance().getSide() == Side.CLIENT )
		{
			MultipartRegistryClient.bindMultipartSpecialRenderer( ChisledBlockPart.class, new ChisledBlockRenderChunkMPSR() );
		}
	}

	@Override
	public void removePartIfPossible(
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

	@Override
	public void triggerPartChange(
			final TileEntity te )
	{
		if ( te instanceof IMultipartContainer && !te.getWorld().isRemote )
		{
			for ( final IMultipart part : ( (IMultipartContainer) te ).getParts() )
			{
				if ( part instanceof ChisledBlockPart )
				{
					( (ChisledBlockPart) part ).notifyPartUpdate();
				}
			}
		}
	}

	@Override
	public void convertIfPossible(
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

	@Override
	public TileEntityBlockChiseled getPartIfPossible(
			final World w,
			final BlockPos pos,
			final boolean create )
	{
		final IMultipartContainer container = MultipartHelper.getOrConvertPartContainer( w, pos, false );

		if ( container != null )
		{
			for ( final IMultipart part : container.getParts() )
			{
				if ( part instanceof ChisledBlockPart )
				{
					return ( (ChisledBlockPart) part ).getTile();
				}
			}

			final ChisledBlockPart part = new ChisledBlockPart();
			if ( MultipartHelper.canAddPart( w, pos, part ) )
			{
				if ( create && !w.isRemote )
				{
					final TileEntityBlockChiseled tx = part.getTile();
					tx.occlusionState = new MultipartContainerBuilder( w, pos, part, container );
					tx.setWorldObj( w );
					tx.setPos( pos );
					return tx;
				}
				else if ( create )
				{
					part.setContainer( container );
					return part.getTile();
				}
			}
		}

		return null;
	}

	@Override
	public boolean isMultiPart(
			final TileEntity te )
	{
		return te instanceof IMultipartContainer || MultipartHelper.canAddPart( te.getWorld(), te.getPos(), new ChisledBlockPart() );
	}

	@Override
	public void addFiler(
			final TileEntity te,
			final VoxelBlob vb )
	{
		if ( isMultiPart( te ) )
		{
			final IMultipartContainer mc = (IMultipartContainer) te;

			final BitCollisionIterator bci = new BitCollisionIterator();
			while ( bci.hasNext() )
			{
				final AxisAlignedBB aabb = new AxisAlignedBB( bci.physicalX, bci.physicalY, bci.physicalZ, bci.physicalX + BitCollisionIterator.One16thf, bci.physicalYp1, bci.physicalZp1 );

				if ( !OcclusionHelper.occlusionTest( mc.getParts(), aabb ) )
				{
					bci.setNext( vb, 1 );
				}
			}
		}
	}

}
