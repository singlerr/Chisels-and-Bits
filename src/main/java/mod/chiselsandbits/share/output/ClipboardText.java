package mod.chiselsandbits.share.output;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import mod.chiselsandbits.blueprints.BlueprintData;
import mod.chiselsandbits.localization.LocalStrings;
import mod.chiselsandbits.localization.LocalizedMessage;
import net.minecraft.client.gui.GuiScreen;

public class ClipboardText implements IShareOutput
{

	String text;

	@Override
	public LocalizedMessage handleOutput(
			final byte[] compressedData,
			final BufferedImage screenshot ) throws UnsupportedEncodingException, IOException
	{
		GuiScreen.setClipboardString( text = getShareString( compressedData ) );
		return new LocalizedMessage( LocalStrings.ShareClipboard );
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

	public static String getShareString(
			byte[] compressedData )
	{
		final StringBuilder o = new StringBuilder();
		o.append( "[C&B](" );
		o.append( Base64.getEncoder().encodeToString( compressedData ) );
		o.append( ")[C&B]" );
		return o.toString();
	}

}
