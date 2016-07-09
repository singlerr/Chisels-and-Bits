package mod.chiselsandbits.share.output;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import mod.chiselsandbits.helpers.LocalStrings;

public class LocalTextFile implements IShareOutput
{

	File outFile;
	final String encoding = "UTF-8";

	public LocalTextFile(
			final File file )
	{
		outFile = file;
	}

	@Override
	public String handleOutput(
			final byte[] compressedData,
			final BufferedImage screenshot ) throws UnsupportedEncodingException, IOException
	{
		final FileOutputStream o = new FileOutputStream( outFile );
		o.write( new String( "[C&B](" ).getBytes( encoding ) );
		o.write( Base64.getEncoder().encodeToString( compressedData ).getBytes( encoding ) );
		o.write( new String( ")[C&B]" ).getBytes( encoding ) );
		o.close();

		return LocalStrings.ShareFile.toString();
	}

}
