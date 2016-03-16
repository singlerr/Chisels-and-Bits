package mod.chiselsandbits.commands;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.core.api.BitAccess;
import mod.chiselsandbits.render.helpers.ModelQuadShare;
import mod.chiselsandbits.render.helpers.ModelUtil;
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
import net.minecraft.world.World;

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

			final Map<ShareMaterial, ShareMaterial> textures = new HashMap<ShareMaterial, ShareMaterial>();
			final StringBuilder output = new StringBuilder( "{\n" );

			final BlockPos min = new BlockPos( Math.min( start.getX(), end.getX() ), Math.min( start.getY(), end.getY() ), Math.min( start.getZ(), end.getZ() ) );
			for ( final MutableBlockPos pos : BlockPos.getAllInBoxMutable( start, end ) )
			{
				IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelFromBlockState( w.getBlockState( pos ), w, pos );
				final BlockPos offset = pos.subtract( min );

				final IBlockState state = w.getBlockState( pos );
				final Item BI = Item.getItemFromBlock( state.getBlock() );
				ItemStack modelStack = BI != null ? new ItemStack( BI, 1, state.getBlock().getDamageValue( w, pos ) ) : null;

				String data = "N/A";
				try
				{
					final BitAccess ba = (BitAccess) ChiselsAndBits.getApi().getBitAccess( w, pos );
					final VoxelBlob blob = ba.getNativeBlob();
					final byte[] bd = blob.blobToBytes( VoxelBlob.VERSION_CROSSWORLD );
					data = Arrays.toString( bd );

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
					// NOPE!
				}

				for ( final EnumFacing face : EnumFacing.VALUES )
				{
					outputFaces( offset, model.getFaceQuads( face ), face, modelStack, textures );
				}

				outputFaces( offset, model.getGeneralQuads(), null, modelStack, textures );

				output.append( "\"source-" ).append( offset.toString() ).append( "\": \"" ).append( data ).append( "\",\n" );
			}

			output.append( "\"textures\": {" );
			final HashSet<TextureAtlasSprite> sprites = new HashSet<TextureAtlasSprite>();

			int materialID = 1;
			for ( final ShareMaterial m : textures.keySet() )
			{
				m.materialID = materialID++;
				sprites.add( m.sprite );
			}

			for ( final TextureAtlasSprite s : sprites )
			{
				output.append( "\"" ).append( System.identityHashCode( s ) ).append( "\": \"" ).append( getIcon( s ) ).append( "\",\n" );
			}

			if ( !sprites.isEmpty() )
			{
				// delete line ending + comma.
				output.deleteCharAt( output.length() - 1 );
				output.deleteCharAt( output.length() - 1 );
			}

			output.append( "},\n\"materials\": {\n" );

			for ( final ShareMaterial m : textures.keySet() )
			{
				output.append( "\"" ).append( m.materialID ).append( "\": [\"" ).append( Integer.toHexString( m.col ) ).append( "\"," ).append( System.identityHashCode( m.sprite ) ).append( "],\n" );
			}

			if ( !textures.values().isEmpty() )
			{
				// delete line ending + comma.
				output.deleteCharAt( output.length() - 1 );
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
			GuiScreen.setClipboardString( modelJSON );
			sender.addChatMessage( new ChatComponentText( "Json Posted to Clipboard" ) );
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

		int materialID = -1;
		public StringBuilder builder;

		public ShareMaterial(
				final TextureAtlasSprite sprite,
				final int col )
		{
			this.sprite = sprite;
			this.col = col;
		}

		@Override
		public int hashCode()
		{
			return sprite.hashCode() ^ col;
		}

		@Override
		public boolean equals(
				final Object obj )
		{
			if ( obj instanceof ShareMaterial )
			{
				final ShareMaterial sm = (ShareMaterial) obj;
				return sprite == sm.sprite && sm.col == col;
			}

			return false;
		}
	};

	private void outputFaces(
			final BlockPos offset,
			final List<BakedQuad> faceQuads,
			final EnumFacing cullFace,
			final ItemStack ItemStack,
			final Map<ShareMaterial, ShareMaterial> textures )
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
					M = new ShareMaterial( sprite, mqr.col );
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
					old.builder.append( ",\n" );
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
