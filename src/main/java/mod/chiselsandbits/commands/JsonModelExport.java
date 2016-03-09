package mod.chiselsandbits.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.helpers.ModelQuadReader;
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

public class JsonModelExport extends CommandBase
{
	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public String getCommandName()
	{
		return "c&b.exportmodel";
	}

	@Override
	public String getCommandUsage(
			final ICommandSender sender )
	{
		return "chiselsandbits.commands.exportjsonmodel.usage";
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
				output.append( "\"" ).append( System.identityHashCode( s ) ).append( "\": \"" ).append( s.getIconName() ).append( "\",\n" );
			}

			if ( !textures.values().isEmpty() )
			{
				// delete line ending + comma.
				output.deleteCharAt( output.length() - 1 );
				output.deleteCharAt( output.length() - 1 );
			}

			output.append( "},\n\"elements\": [\n" );

			for ( final String json : textures.values() )
			{
				output.append( json );
			}

			if ( !textures.values().isEmpty() )
			{
				// delete line ending + comma.
				output.deleteCharAt( output.length() - 1 );
				output.deleteCharAt( output.length() - 1 );
			}

			output.append( "\n],\n\"display\": { \"thirdperson\": { \"rotation\": [ 10, -45, 170 ], \"translation\": [ 0, 1.5, -2.75 ], \"scale\": [ 0.375, 0.375, 0.375 ] } } }" );

			final String modelJSON = output.toString();
			GuiScreen.setClipboardString( modelJSON );
			sender.addChatMessage( new ChatComponentText( "Json Posted to Clipboard" ) );
		}
		else
		{
			sender.addChatMessage( new ChatComponentText( "No Item in Hand." ) );
		}
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

				final ModelQuadReader mqr = new ModelQuadReader( "#" + System.identityHashCode( sprite ), sprite, quad.getFace(), cullFace );
				quad.pipe( mqr );
				final String newJSON = mqr.toString();

				String old = textures.get( sprite );
				if ( old == null )
				{
					old = "";
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
