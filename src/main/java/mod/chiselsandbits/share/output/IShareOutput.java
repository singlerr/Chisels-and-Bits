package mod.chiselsandbits.share.output;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface IShareOutput
{

	String handleOutput(
			byte[] compressedData,
			BufferedImage screenshot ) throws UnsupportedEncodingException, IOException;

}
