package mod.chiselsandbits.share.output;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.activation.DataHandler;

import mod.chiselsandbits.blueprints.BlueprintData;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.share.ScreenShotEncoder;

public class ClipboardImage implements IShareOutput
{

	byte[] data = null;

	public ClipboardImage()
	{
	}

	@Override
	public String handleOutput(
			final byte[] compressedData,
			final BufferedImage screenshot ) throws UnsupportedEncodingException, IOException
	{
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		ScreenShotEncoder.encodeScreenshot( screenshot, compressedData, s );

		data = s.toByteArray();
		DataHandler dataHandler = new DataHandler( s.toByteArray(), "image/png" );
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents( dataHandler, null );

		return LocalStrings.ShareFile.toString();
	}

	@Override
	public BlueprintData getData()
	{
		BlueprintData bpd = new BlueprintData( null );

		try
		{
			bpd.loadData( new ByteArrayInputStream( data ) );
		}
		catch ( IOException e )
		{

		}

		return bpd;
	}

}
