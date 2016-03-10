package mod.chiselsandbits.commands;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.helpers.ModelQuadShare;
import mod.chiselsandbits.render.helpers.ModelUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;

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

	@Override
	public void processCommand(
			final ICommandSender sender,
			final String[] args ) throws CommandException
	{
		final ItemStack is = ClientSide.instance.getPlayer().getCurrentEquippedItem();
		if ( is != null && is.getItem() != null )
		{
			final IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel( is );

			final Map<TextureAtlasSprite, String> textures = new HashMap<TextureAtlasSprite, String>();

			for ( final EnumFacing face : EnumFacing.VALUES )
			{
				outputFaces( model.getFaceQuads( face ), face, textures );
			}

			outputFaces( model.getGeneralQuads(), null, textures );

			String data = "N/A";

			if ( is.getItem() instanceof ItemBlockChiseled && is.hasTagCompound() )
			{
				final VoxelBlob blob = ModUtil.getBlobFromStack( is, null );

				final byte[] bd = blob.blobToBytes( VoxelBlob.VERSION_CROSSWORLD );
				data = Arrays.toString( bd );
			}

			final StringBuilder output = new StringBuilder( "{ \"source\": \"" ).append( data ).append( "\",\n\"textures\": {" );
			for ( final TextureAtlasSprite s : textures.keySet() )
			{
				output.append( "\"" ).append( System.identityHashCode( s ) ).append( "\": \"" ).append( getIcon( s ) ).append( "\",\n" );
			}

			if ( !textures.values().isEmpty() )
			{
				// delete line ending + comma.
				output.deleteCharAt( output.length() - 1 );
				output.deleteCharAt( output.length() - 1 );
			}

			output.append( "},\n\"model\": [\n" );

			int off = 0;
			for ( final String json : textures.values() )
			{
				if ( off++ > 0 )
				{
					output.append( ",\n" );
				}

				output.append( json );
			}

			output.append( "\n] }" );

			final String modelJSON = output.toString();
			GuiScreen.setClipboardString( modelJSON );
			sender.addChatMessage( new ChatComponentText( "Json Posted to Clipboard" ) );
		}
		else
		{
			sender.addChatMessage( new ChatComponentText( "No Item in Hand." ) );
		}
	}

	private static BufferedImage getIconAsImage(
			final TextureAtlasSprite textureAtlasSprite )
	{
		final int width = textureAtlasSprite.getIconWidth();
		final int height = textureAtlasSprite.getIconHeight();

		final BufferedImage bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_4BYTE_ABGR );

		final int[] frameData = textureAtlasSprite.getFrameTextureData( 0 )[0];
		bufferedImage.setRGB( 0, 0, width, height, frameData, 0, width );

		// texture packs are too big... down sample to at least 32x, this
		// preserves fluids as well...
		if ( width > 32 )
		{
			final Image resampled = bufferedImage.getScaledInstance( 32, -1, BufferedImage.SCALE_FAST );

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

	private void outputFaces(
			final List<BakedQuad> faceQuads,
			final EnumFacing cullFace,
			final Map<TextureAtlasSprite, String> textures )
	{
		for ( final BakedQuad quad : faceQuads )
		{
			try
			{
				final TextureAtlasSprite sprite = ModelUtil.findQuadTexture( quad );

				final ModelQuadShare mqr = new ModelQuadShare( "" + System.identityHashCode( sprite ), sprite, quad.getFace(), cullFace );
				quad.pipe( mqr );
				final String newJSON = mqr.toString();

				String old = textures.get( sprite );

				if ( old == null )
				{
					old = "";
				}
				else
				{
					old += ",\n";
				}

				textures.put( sprite, old + newJSON );
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
