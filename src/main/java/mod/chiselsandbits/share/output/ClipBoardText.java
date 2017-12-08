package mod.chiselsandbits.share.output;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import mod.chiselsandbits.blueprints.BlueprintData;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.client.gui.GuiScreen;

public class ClipBoardText implements IShareOutput
{

	String text;

	@Override
	public String handleOutput(
			final byte[] compressedData,
			final BufferedImage screenshot ) throws UnsupportedEncodingException, IOException
	{
		final StringBuilder o = new StringBuilder();
		o.append( "[C&B](" );
		o.append( Base64.getEncoder().encodeToString( compressedData ) );
		o.append( ")[C&B]" );
		GuiScreen.setClipboardString( text = o.toString() );

		return LocalStrings.ShareClipboard.toString();
	}

	@Override
	public BlueprintData getData()
	{
		BlueprintData bpd = new BlueprintData( null );

		try
		{
			bpd.loadData( new ByteArrayInputStream( text.getBytes( "UTF-8" ) ) );
		}
		catch ( IOException e )
		{

		}

		return bpd;
	}

}
