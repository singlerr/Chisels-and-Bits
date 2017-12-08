package mod.chiselsandbits.share.output;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.Nonnull;

import mod.chiselsandbits.blueprints.BlueprintData;

public interface IShareOutput
{

	String handleOutput(
			byte[] compressedData,
			BufferedImage screenshot ) throws UnsupportedEncodingException, IOException;

	@Nonnull
	BlueprintData getData();

}
