package mod.chiselsandbits.commands;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageIO;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.core.api.BitAccess;
import mod.chiselsandbits.render.chiseledblock.ChiselLayer;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import mod.chiselsandbits.render.helpers.ModelQuadShare;
import mod.chiselsandbits.render.helpers.ModelQuadShare.ShareFaces;
import mod.chiselsandbits.render.helpers.ModelUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ISmartBlockModel;

/**
 * This is a WIP, and will eventually be incorporated into a gameplay element.
 */
public class Share extends CommandBase
{
	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public String getCommandName()
	{
		return "c&b.share";
	}

	@Override
	public String getCommandUsage(
			final ICommandSender sender )
	{
		return "chiselsandbits.commands.share.usage";
	}

	BlockPos start;

	private byte[] preFixByteArray(
			final byte[] prefix,
			final byte[] main )
	{
		final byte[] c = new byte[prefix.length + main.length];
		System.arraycopy( prefix, 0, c, 0, prefix.length );
		System.arraycopy( main, 0, c, prefix.length, main.length );
		return c;
	}

	@Override
	public void processCommand(
			final ICommandSender sender,
			final String[] args ) throws CommandException
	{
		if ( args.length > 0 && args[0].equals( "start" ) )
		{
			start = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
			sender.addChatMessage( new ChatComponentText( "Start Pos Set" ) );
		}
		else if ( start == null )
		{
			sender.addChatMessage( new ChatComponentText( "Start Pos Not Set Yet, use argument 'start'." ) );
		}
		else if ( start != null )
		{
			final World w = Minecraft.getMinecraft().theWorld;

			BlockPos end = Minecraft.getMinecraft().objectMouseOver.getBlockPos();

			if ( end == null )
			{
				end = Minecraft.getMinecraft().thePlayer.getPosition();
			}

			start = new BlockPos( 295, 60, 246 );
			end = new BlockPos( 325, 141, 278 );

			final EnumWorldBlockLayer[] layers = EnumWorldBlockLayer.values();

			final Map<ShareMaterial, ShareMaterial> textures = new HashMap<ShareMaterial, ShareMaterial>();

			final ShareFormatWriter writer = new ShareFormatWriter();

			// version
			writer.writeInt( 1 );

			final BlockPos min = new BlockPos( Math.min( start.getX(), end.getX() ), Math.min( start.getY(), end.getY() ), Math.min( start.getZ(), end.getZ() ) );
			final BlockPos max = new BlockPos( Math.max( start.getX(), end.getX() ), Math.max( start.getY(), end.getY() ), Math.max( start.getZ(), end.getZ() ) );

			final int xSize = max.getX() - min.getX() + 1;
			final int ySize = max.getY() - min.getY() + 1;
			final int zSize = max.getZ() - min.getZ() + 1;
			final int xySize = xSize * ySize;

			writer.writeInt( xSize );
			writer.writeInt( ySize );
			writer.writeInt( zSize );
			final int[] stucture = new int[xSize * ySize * zSize];

			int modelNum = 1;
			final HashMap<byte[], Integer> models = new HashMap<byte[], Integer>();

			final ChiseledBlockSmartModel cbsm = new ChiseledBlockSmartModel();

			final ChiselLayer[] single = new ChiselLayer[1];
			final ChiselLayer[] solid = new ChiselLayer[2];
			solid[0] = ChiselLayer.SOLID;
			solid[1] = ChiselLayer.SOLID_FLUID;

			final byte[] badBlock = new byte[] { 0 };
			final byte[] blockprefix = new byte[] { 1 };
			final byte[] cbprefix = new byte[] { 2 };

			for ( final MutableBlockPos pos : BlockPos.getAllInBoxMutable( start, end ) )
			{
				IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelFromBlockState( w.getBlockState( pos ), w, pos );
				final BlockPos offset = pos.subtract( min );
				final IBlockState state = w.getBlockState( pos );

				final Item BI = Item.getItemFromBlock( state.getBlock() );
				ItemStack modelStack = BI != null ? new ItemStack( BI, 1, state.getBlock().getDamageValue( w, pos ) ) : null;

				byte[] data;
				try
				{
					data = preFixByteArray( blockprefix, Block.blockRegistry.getNameForObject( state.getBlock() ).toString().getBytes( "UTF-8" ) );
				}
				catch ( final UnsupportedEncodingException e1 )
				{
					data = badBlock;
				}

				try
				{
					final BitAccess ba = (BitAccess) ChiselsAndBits.getApi().getBitAccess( w, pos );
					final VoxelBlob blob = ba.getNativeBlob();
					final byte[] bd = preFixByteArray( cbprefix, blob.blobToBytes( VoxelBlob.VERSION_CROSSWORLD ) );
					data = bd;

					final ItemStack is = ba.getBitsAsItem( null, ItemType.CHISLED_BLOCK );

					if ( is == null )
					{
						continue;
					}

					modelStack = is;
					model = cbsm;
				}
				catch ( final CannotBeChiseled e )
				{
				}

				Integer cm = models.get( data );
				if ( cm == null )
				{
					models.put( data, cm = modelNum++ );
				}

				stucture[offset.getX() + offset.getY() * xSize + offset.getZ() * xySize] = cm;

				for ( final EnumWorldBlockLayer layer : layers )
				{
					// test the block for the layer.
					final Block blk = state.getBlock();
					if ( !blk.canRenderInLayer( layer ) )
					{
						continue;
					}

					IBakedModel activeModel = model;

					if ( activeModel instanceof ChiseledBlockSmartModel )
					{
						single[0] = ChiselLayer.fromLayer( layer, false );
						activeModel = ( (ChiseledBlockSmartModel) model ).handleItemState( modelStack, layer == EnumWorldBlockLayer.SOLID ? solid : single );
					}
					else if ( activeModel instanceof ISmartBlockModel )
					{
						activeModel = ( (ISmartBlockModel) model ).handleBlockState( blk.getExtendedState( state, w, pos ) );
					}

					for ( final EnumFacing face : EnumFacing.VALUES )
					{
						final BlockPos p = pos.offset( face );
						final boolean isEdge = p.getX() < min.getX() ||
								p.getX() > max.getX() ||
								p.getY() < min.getY() ||
								p.getY() > max.getY() ||
								p.getZ() < min.getZ() ||
								p.getZ() > max.getZ();

						if ( blk.shouldSideBeRendered( w, p, face ) || isEdge )
						{
							outputFaces( offset, activeModel.getFaceQuads( face ), face, modelStack, textures, layer );
						}
					}

					outputFaces( offset, activeModel.getGeneralQuads(), null, modelStack, textures, layer );
				}
			}

			final int bits = Math.max( 1, Integer.SIZE - Integer.numberOfLeadingZeros( models.size() - 1 ) );

			// how many bits per structure element?
			writer.writeInt( bits ); // bits per stucture

			// write structure, has x*y*z elements.
			for ( int si = 0; si < stucture.length; ++si )
			{
				writer.writeBits( stucture[si], bits );
			}

			final byte[][] orderedList = new byte[models.size()][];
			for ( final Entry<byte[], Integer> en : models.entrySet() )
			{
				orderedList[en.getValue() - 1] = en.getKey();
			}

			writer.writeInt( orderedList.length ); // model count.
			for ( int si = 0; si < orderedList.length; ++si )
			{
				writer.writeBytes( orderedList[si] );
			}

			final HashSet<TextureAtlasSprite> sprites = new HashSet<TextureAtlasSprite>();

			int materialID = 0;
			for ( final ShareMaterial m : textures.keySet() )
			{
				m.materialID = materialID++;
				sprites.add( m.sprite );
			}

			writer.writeInt( sprites.size() ); // texture count.
			for ( final TextureAtlasSprite s : sprites )
			{
				writer.writeInt( System.identityHashCode( s ) ); // texture id
				writer.writeBytes( getIcon( s ) );
			}

			final ShareMaterial[] orderedMaterials = new ShareMaterial[textures.size()];
			for ( final ShareMaterial m : textures.keySet() )
			{
				orderedMaterials[m.materialID] = m;
			}

			writer.writeInt( orderedMaterials.length ); // model count.
			for ( int si = 0; si < orderedMaterials.length; ++si )
			{
				final ShareMaterial m = orderedMaterials[si];
				writer.writeInt( m.col );
				writer.writeInt( getLayerName( m.layer ) );
				writer.writeInt( System.identityHashCode( m.sprite ) );
			}

			writer.writeInt( textures.size() ); // face groups...
			for ( final ShareMaterial json : textures.values() )
			{
				json.writeOut( writer );
			}

			final byte[] jsonData = writer.inner.toByteArray();

			final ByteArrayOutputStream byteStream = new ByteArrayOutputStream( jsonData.length );
			try
			{
				final DeflaterOutputStream zipStream = new DeflaterOutputStream( byteStream );
				try
				{
					zipStream.write( jsonData );
				}
				finally
				{
					zipStream.close();
				}
			}
			catch ( final IOException e )
			{
				Log.logError( "Error Deflating Data", e );
			}
			finally
			{
				try
				{
					byteStream.close();
				}
				catch ( final IOException e )
				{
					Log.logError( "Error Deflating Data", e );
				}
			}

			final byte[] compressedData = byteStream.toByteArray();
			final byte[] apacheBytes = org.apache.commons.codec.binary.Base64.encodeBase64( compressedData );

			GuiScreen.setClipboardString( new String( apacheBytes ) );
			sender.addChatMessage( new ChatComponentText( "Json Posted to Clipboard" ) );
		}
	}

	/**
	 * 0 - solid 1 - alpha 2 - translucent
	 * 
	 * @param layer
	 * @return
	 */
	private int getLayerName(
			final EnumWorldBlockLayer layer )
	{
		switch ( layer )
		{
			case CUTOUT:
				return 1;

			case CUTOUT_MIPPED:
				return 1;

			case TRANSLUCENT:
				return 2;

			case SOLID:
			default:
				return 0;
		}
	}

	private static BufferedImage getIconAsImage(
			final TextureAtlasSprite textureAtlasSprite )
	{
		final int width = textureAtlasSprite.getIconWidth();
		final int height = textureAtlasSprite.getIconHeight();

		final BufferedImage bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_4BYTE_ABGR );

		int[] frameData;

		try
		{
			frameData = textureAtlasSprite.getFrameTextureData( 0 )[0];
		}
		catch ( final Exception e )
		{
			frameData = ClientSide.instance.getMissingIcon().getFrameTextureData( 0 )[0];
		}

		bufferedImage.setRGB( 0, 0, width, height, frameData, 0, width );

		// texture packs are too big... down sample to at least 32x, this
		// preserves fluids as well...
		if ( width > 32 )
		{
			final Image resampled = bufferedImage.getScaledInstance( 32, -1, Image.SCALE_FAST );

			final BufferedImage newImage = new BufferedImage( resampled.getWidth( null ), resampled.getHeight( null ), BufferedImage.TYPE_4BYTE_ABGR );
			final Graphics g = newImage.getGraphics();
			g.drawImage( resampled, 0, 0, null );
			g.dispose();

			return newImage;
		}

		return bufferedImage;
	}

	private byte[] getIcon(
			final TextureAtlasSprite s )
	{
		final ByteArrayOutputStream data = new ByteArrayOutputStream();

		try
		{
			ImageIO.write( getIconAsImage( s ), "png", data );
		}
		catch ( final IOException e )
		{
			return org.apache.commons.codec.binary.Base64.decodeBase64( "R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=" );
		}

		return data.toByteArray();
	}

	private static class ShareMaterial
	{

		TextureAtlasSprite sprite;
		int col;
		EnumWorldBlockLayer layer;

		ArrayList<ModelQuadShare.ShareFaces> faces = null;

		int materialID = -1;

		public ShareMaterial(
				final TextureAtlasSprite sprite,
				final int col,
				final EnumWorldBlockLayer layer )
		{
			this.sprite = sprite;
			this.col = col;
			this.layer = layer;
		}

		public void writeOut(
				final ShareFormatWriter writer )
		{
			for ( final ShareFaces f : faces )
			{
				writer.writeInt( f.x );
				writer.writeInt( f.y );
				writer.writeInt( f.z );
				writer.writeBits( f.u, 5 );
				writer.writeBits( f.v, 5 );
			}
		}

		@Override
		public int hashCode()
		{
			return sprite.hashCode() ^ col ^ layer.hashCode();
		}

		@Override
		public boolean equals(
				final Object obj )
		{
			if ( obj instanceof ShareMaterial )
			{
				final ShareMaterial sm = (ShareMaterial) obj;
				return sprite == sm.sprite && sm.col == col && sm.layer == layer;
			}

			return false;
		}
	};

	private void outputFaces(
			final BlockPos offset,
			final List<BakedQuad> faceQuads,
			final EnumFacing cullFace,
			final ItemStack ItemStack,
			final Map<ShareMaterial, ShareMaterial> textures,
			final EnumWorldBlockLayer layer )
	{
		ShareMaterial M = null;
		ShareMaterial old = null;
		for ( final BakedQuad quad : faceQuads )
		{
			try
			{
				final TextureAtlasSprite sprite = ModelUtil.findQuadTexture( quad );

				final ModelQuadShare mqr = new ModelQuadShare( "" + System.identityHashCode( sprite ), offset, sprite, quad.getFace(), cullFace, ItemStack );
				quad.pipe( mqr );
				final String newJSON = mqr.toString();

				if ( M == null || M.col != mqr.col || M.sprite != sprite || M.layer != layer )
				{
					M = new ShareMaterial( sprite, mqr.col, layer );
					old = textures.get( M );
				}

				if ( old == null )
				{
					old = M;
					M.faces = new ArrayList<ShareFaces>();
					M = textures.put( M, old );
				}

				mqr.getFaces( old.faces );

			}
			catch ( final IllegalArgumentException e )
			{
				e.printStackTrace();
			}
			catch ( final IllegalAccessException e )
			{
				e.printStackTrace();
			}
			catch ( final NullPointerException e )
			{
				e.printStackTrace();
			}
		}
	}
}
