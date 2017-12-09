package mod.chiselsandbits.share.output;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.Nonnull;

import mod.chiselsandbits.blueprints.BlueprintData;
import mod.chiselsandbits.localization.LocalizedMessage;

public interface IShareOutput
{

	LocalizedMessage handleOutput(
			byte[] compressedData,
			BufferedImage screenshot ) throws UnsupportedEncodingException, IOException;

	@Nonnull
	BlueprintData getData();

}
