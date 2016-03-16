package mod.chiselsandbits.commands;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import mod.chiselsandbits.render.helpers.ModelQuadShare;
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

			final EnumWorldBlockLayer[] layers = EnumWorldBlockLayer.values();

			final Map<ShareMaterial, ShareMaterial> textures = new HashMap<ShareMaterial, ShareMaterial>();
			final StringBuilder output = new StringBuilder( "{\"version\":\"1\",\"sources\":{\n" );

			final BlockPos min = new BlockPos( Math.min( start.getX(), end.getX() ), Math.min( start.getY(), end.getY() ), Math.min( start.getZ(), end.getZ() ) );
			final BlockPos max = new BlockPos( Math.max( start.getX(), end.getX() ), Math.max( start.getY(), end.getY() ), Math.max( start.getZ(), end.getZ() ) );

			int modelNum = 1;
			final HashMap<String, Integer> models = new HashMap<String, Integer>();

			for ( final MutableBlockPos pos : BlockPos.getAllInBoxMutable( start, end ) )
			{
				IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelFromBlockState( w.getBlockState( pos ), w, pos );
				final BlockPos offset = pos.subtract( min );
				final IBlockState state = w.getBlockState( pos );

				final Item BI = Item.getItemFromBlock( state.getBlock() );
				ItemStack modelStack = BI != null ? new ItemStack( BI, 1, state.getBlock().getDamageValue( w, pos ) ) : null;

				String data = Block.blockRegistry.getNameForObject( state.getBlock() ).toString();

				try
				{
					final BitAccess ba = (BitAccess) ChiselsAndBits.getApi().getBitAccess( w, pos );
					final VoxelBlob blob = ba.getNativeBlob();
					final byte[] bd = blob.blobToBytes( VoxelBlob.VERSION_CROSSWORLD );
					data = bytesToString( bd );

					final ItemStack is = ba.getBitsAsItem( null, ItemType.CHISLED_BLOCK );

					if ( is == null )
					{
						continue;
					}

					modelStack = is;
					model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel( is );
				}
				catch ( final CannotBeChiseled e )
				{
				}

				Integer cm = models.get( data );
				if ( cm == null )
				{
					models.put( data, cm = modelNum++ );
				}

				output.append( "\"" ).append( posAsString( offset ) ).append( "\":" ).append( cm ).append( "," );

				for ( final EnumWorldBlockLayer layer : layers )
				{
					// test the block for the layer.
					if ( !state.getBlock().canRenderInLayer( layer ) )
					{
						continue;
					}

					IBakedModel activeModel = model;

					if ( activeModel instanceof ChiseledBlockSmartModel )
					{
						activeModel = ( (ChiseledBlockSmartModel) model ).handleBlockState( state.getBlock().getExtendedState( state, w, pos ), layer );
					}
					else if ( activeModel instanceof ISmartBlockModel )
					{
						activeModel = ( (ISmartBlockModel) model ).handleBlockState( state.getBlock().getExtendedState( state, w, pos ) );
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

						if ( w.getBlockState( p ).getBlock().shouldSideBeRendered( w, p, face ) || isEdge )
						{
							outputFaces( offset, activeModel.getFaceQuads( face ), face, modelStack, textures, layer );
						}
					}

					outputFaces( offset, activeModel.getGeneralQuads(), null, modelStack, textures, layer );
				}
			}

			if ( !models.values().isEmpty() )
			{
				// delete line ending + comma.
				output.deleteCharAt( output.length() - 1 );
			}

			output.append( "},\n\"types\": {" );

			for ( final Entry<String, Integer> en : models.entrySet() )
			{
				output.append( "\"" ).append( en.getValue() ).append( "\": \"" ).append( en.getKey() ).append( "\"," );
			}

			if ( !models.entrySet().isEmpty() )
			{
				// delete line ending + comma.
				output.deleteCharAt( output.length() - 1 );
			}

			output.append( "},\n\"textures\": {" );

			final HashSet<TextureAtlasSprite> sprites = new HashSet<TextureAtlasSprite>();

			int materialID = 1;
			for ( final ShareMaterial m : textures.keySet() )
			{
				m.materialID = materialID++;
				sprites.add( m.sprite );
			}

			for ( final TextureAtlasSprite s : sprites )
			{
				output.append( "\"" ).append( System.identityHashCode( s ) ).append( "\": \"" ).append( getIcon( s ) ).append( "\"," );
			}

			if ( !sprites.isEmpty() )
			{
				// delete line ending + comma.
				output.deleteCharAt( output.length() - 1 );
			}

			output.append( "},\n\"materials\": {\n" );

			for ( final ShareMaterial m : textures.keySet() )
			{
				output.append( "\"" ).append( m.materialID ).append( "\": [\"" ).append( getLayerName( m.layer ) ).append( "\",\"" ).append( Integer.toHexString( m.col ) ).append( "\"," ).append( System.identityHashCode( m.sprite ) )
						.append( "]," );
			}

			if ( !textures.values().isEmpty() )
			{
				// delete line ending + comma.
				output.deleteCharAt( output.length() - 1 );
			}

			output.append( "},\n\"model\": [\n" );

			for ( final ShareMaterial json : textures.values() )
			{
				output.append( "\n{\"m\":" ).append( json.materialID ).append( ",\"f\":[" );

				output.append( json.builder );
				output.append( "]}," );
			}

			if ( !textures.values().isEmpty() )
			{
				// delete comma.
				output.deleteCharAt( output.length() - 1 );
			}

			output.append( "\n] }" );

			final String modelJSON = output.toString();
			byte[] jsonData;
			try
			{
				jsonData = modelJSON.getBytes( "UTF-8" );

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
			catch ( final UnsupportedEncodingException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private String bytesToString(
			final byte[] bd )
	{
		final StringBuilder o = new StringBuilder( "C&B:" );

		for ( final byte b : bd )
		{
			final String hex = Integer.toHexString( b );

			if ( hex.length() == 0 )
			{
				o.append( "00" );
			}

			if ( hex.length() == 1 )
			{
				o.append( "0" );
			}

			o.append( hex );
		}

		return o.toString();
	}

	private Object posAsString(
			final BlockPos offset )
	{
		return new StringBuilder().append( Integer.toString( offset.getX(), 36 ) ).append( ',' ).append( Integer.toString( offset.getY(), 36 ) ).append( ',' ).append( Integer.toString( offset.getZ(), 36 ) ).toString();
	}

	private Object getLayerName(
			final EnumWorldBlockLayer layer )
	{
		switch ( layer )
		{
			case CUTOUT:
				return "a";

			case CUTOUT_MIPPED:
				return "a";

			case TRANSLUCENT:
				return "t";

			case SOLID:
			default:
				return "s";
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

	private String getIcon(
			final TextureAtlasSprite s )
	{
		final ByteArrayOutputStream data = new ByteArrayOutputStream();

		try
		{
			ImageIO.write( getIconAsImage( s ), "png", data );
		}
		catch ( final IOException e )
		{
			return "data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=";
		}

		final byte[] apacheBytes = org.apache.commons.codec.binary.Base64.encodeBase64( data.toByteArray() );
		return "data:image/png;base64," + new String( apacheBytes );
	}

	private static class ShareMaterial
	{

		TextureAtlasSprite sprite;
		int col;
		EnumWorldBlockLayer layer;

		int materialID = -1;
		public StringBuilder builder;

		public ShareMaterial(
				final TextureAtlasSprite sprite,
				final int col,
				final EnumWorldBlockLayer layer )
		{
			this.sprite = sprite;
			this.col = col;
			this.layer = layer;
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

				if ( M == null || M.col != mqr.col || M.sprite != sprite )
				{
					M = new ShareMaterial( sprite, mqr.col, layer );
					old = textures.get( M );
				}

				if ( old == null )
				{
					old = M;
					M.builder = new StringBuilder( "" );
					textures.put( M, old );
				}
				else
				{
					old.builder.append( "," );
				}

				old.builder.append( newJSON );

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
