package mod.chiselsandbits.share.output;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.share.ScreenShotEncoder;

public class LocalPNGFile implements IShareOutput
{
	File outFile;

	public LocalPNGFile(
			final File file )
	{
		outFile = file;
	}

	@Override
	public String handleOutput(
			final byte[] compressedData,
			final BufferedImage screenshot ) throws UnsupportedEncodingException, IOException
	{
		ScreenShotEncoder.encodeScreenshot( screenshot, compressedData, outFile );
		return LocalStrings.ShareFile.toString();
	}

}
