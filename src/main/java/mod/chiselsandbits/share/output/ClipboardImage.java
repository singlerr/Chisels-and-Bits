package mod.chiselsandbits.share.output;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import mod.chiselsandbits.blueprints.BlueprintData;
import mod.chiselsandbits.localization.LocalStrings;
import mod.chiselsandbits.localization.LocalizedMessage;
import mod.chiselsandbits.share.ScreenShotEncoder;

// this won't work, java refuses to support alpha channel on clipboard.
public class ClipboardImage implements IShareOutput
{

	byte[] data = null;

	public ClipboardImage()
	{
	}

	@Override
	public LocalizedMessage handleOutput(
			final byte[] compressedData,
			final BufferedImage screenshot ) throws UnsupportedEncodingException, IOException
	{
		BufferedImage image = ScreenShotEncoder.encodeScreenshot( screenshot, compressedData );

		ImageTransferable transferable = new ImageTransferable( image );
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents( transferable, null );

		// save text version to game blueprint.
		final StringBuilder o = new StringBuilder();
		o.append( "[C&B](" );
		o.append( Base64.getEncoder().encodeToString( compressedData ) );
		o.append( ")[C&B]" );
		data = o.toString().getBytes( "UTF-8" );

		return new LocalizedMessage( LocalStrings.ShareClipboard );
	}

	static class ImageTransferable implements Transferable
	{
		private Image image;

		public ImageTransferable(
				Image image )
		{
			this.image = image;
		}

		public Object getTransferData(
				DataFlavor flavor )
				throws UnsupportedFlavorException
		{
			if ( isDataFlavorSupported( flavor ) )
			{
				return image;
			}
			else
			{
				throw new UnsupportedFlavorException( flavor );
			}
		}

		public boolean isDataFlavorSupported(
				DataFlavor flavor )
		{
			return flavor == DataFlavor.imageFlavor;
		}

		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

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
