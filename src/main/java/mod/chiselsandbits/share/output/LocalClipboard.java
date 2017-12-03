package mod.chiselsandbits.share.output;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.client.gui.GuiScreen;

public class LocalClipboard implements IShareOutput
{

	@Override
	public String handleOutput(
			final byte[] compressedData,
			final BufferedImage screenshot ) throws UnsupportedEncodingException, IOException
	{
		final StringBuilder o = new StringBuilder();
		o.append( "[C&B](" );
		o.append( Base64.getEncoder().encodeToString( compressedData ) );
		o.append( ")[C&B]" );
		GuiScreen.setClipboardString( o.toString() );

		return LocalStrings.ShareClipboard.toString();
	}

}
