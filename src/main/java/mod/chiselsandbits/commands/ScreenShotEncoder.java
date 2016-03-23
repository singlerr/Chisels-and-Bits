package mod.chiselsandbits.commands;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;

public class ScreenShotEncoder
{

	public static BufferedImage getScreenshot()
	{
		int width = Minecraft.getMinecraft().displayWidth;
		int height = Minecraft.getMinecraft().displayHeight;
		final Framebuffer buffer = Minecraft.getMinecraft().getFramebuffer();

		if ( OpenGlHelper.isFramebufferEnabled() )
		{
			width = buffer.framebufferTextureWidth;
			height = buffer.framebufferTextureHeight;
		}

		final int pixels = width * height;
		final IntBuffer pixelBuffer = BufferUtils.createIntBuffer( pixels );
		final int[] pixelValues = new int[pixels];

		GL11.glPixelStorei( GL11.GL_PACK_ALIGNMENT, 1 );
		GL11.glPixelStorei( GL11.GL_UNPACK_ALIGNMENT, 1 );
		pixelBuffer.clear();

		if ( OpenGlHelper.isFramebufferEnabled() )
		{
			GlStateManager.bindTexture( buffer.framebufferTexture );
			GL11.glGetTexImage( GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer );
		}
		else
		{
			GL11.glReadPixels( 0, 0, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer );
		}

		pixelBuffer.get( pixelValues );
		TextureUtil.processPixelValues( pixelValues, width, height );
		BufferedImage screenshotImage = null;

		if ( OpenGlHelper.isFramebufferEnabled() )
		{
			screenshotImage = new BufferedImage( buffer.framebufferWidth, buffer.framebufferHeight, BufferedImage.TYPE_INT_RGB );
			final int textureFrameDiff = buffer.framebufferTextureHeight - buffer.framebufferHeight;

			for ( int y = textureFrameDiff; y < buffer.framebufferTextureHeight; ++y )
			{
				for ( int x = 0; x < buffer.framebufferWidth; ++x )
				{
					screenshotImage.setRGB( x, y - textureFrameDiff, pixelValues[y * buffer.framebufferTextureWidth + x] );
				}
			}
		}
		else
		{
			screenshotImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
			screenshotImage.setRGB( 0, 0, width, height, pixelValues, 0, width );
		}

		return screenshotImage;
	}

	static public void ScreenShotEncoder(
			final BufferedImage screenshot,
			final byte[] modelData )
	{
		final int sizeOfData = 4 + modelData.length;

		final int extraStoragePerPixel = 3;
		final int storagePerImagePixel = 1;

		final int width = screenshot.getWidth();
		final int height = screenshot.getHeight();

		final int innerImageStorage = width * height * storagePerImagePixel;

		int extraXStorage = 0;
		int extraYStorage = 0;

		int newXData = 0;
		int newYData = 0;

		while ( sizeOfData > innerImageStorage + extraXStorage + extraYStorage )
		{
			if ( newYData < newXData )
			{
				newYData += 2;
				extraYStorage += extraStoragePerPixel * ( width + newXData );
			}
			else
			{
				newXData += 2;
				extraXStorage += extraStoragePerPixel * ( height + newYData );
			}
		}

		final BufferedImage output = new BufferedImage( width + newXData, height + newYData, BufferedImage.TYPE_4BYTE_ABGR );

		final int zeros[] = new int[output.getWidth() * output.getHeight() * 4];
		output.setRGB( 0, 0, output.getWidth(), output.getHeight(), zeros, 0, output.getWidth() * 4 );

		// transfer screenshot to output...
		final Graphics g = output.getGraphics();
		g.drawImage( screenshot, newXData / 2, newYData / 2, null );
		g.dispose();

		final int size = modelData.length;

		final ImageWriter writer = new ImageWriter( output, storagePerImagePixel, extraStoragePerPixel );

		// time to encode data..
		writer.writeByte( size >> 24 );
		writer.writeByte( size >> 16 & 0xff );
		writer.writeByte( size >> 8 & 0xff );
		writer.writeByte( size & 0xff );

		for ( int x = 0; x < size; ++x )
		{
			writer.writeByte( modelData[x] & 0xff );
		}

		final JFileChooser fc = new JFileChooser();
		final File f = new File( fc.getFileSystemView().getDefaultDirectory().toString(), "shared.png" );
		try
		{
			ImageIO.write( output, "png", f );
		}
		catch ( final IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static class ImageWriter
	{

		final int storagePerImagePixel;
		final int extraStoragePerPixel;

		int x = 0;
		int y = 0;
		int innerPixel = 0;

		int width;
		final BufferedImage image;

		public ImageWriter(
				final BufferedImage output,
				final int storagePerImagePixel,
				final int extraStoragePerPixel )
		{
			image = output;
			width = image.getWidth();
			this.storagePerImagePixel = storagePerImagePixel;
			this.extraStoragePerPixel = extraStoragePerPixel;

			if ( extraStoragePerPixel != 3 )
			{
				throw new RuntimeException( "requires 3!" );
			}
		}

		private void writeByte(
				final int iByte )
		{
			int rgba = image.getRGB( x, y );
			int alpha = rgba >>> 24;

			// extra...
			if ( alpha < 128 )
			{
				rgba |= iByte << 16 - innerPixel;
				image.setRGB( x, y, rgba );

				innerPixel += 8;
				if ( innerPixel >= 24 )
				{
					innerPixel = 0;
					++x;

					if ( x >= width )
					{
						x = 0;
						++y;
					}
				}
			}
			else // inner image
			{
				int red = rgba >>> 16 & 0xfc;
				int green = rgba >>> 8 & 0xfc;
				int blue = rgba & 0xfc;
				alpha &= 0xfc;

				final int p1 = iByte >>> 6 & 3;
				final int p2 = iByte >>> 4 & 3;
				final int p3 = iByte >>> 2 & 3;
				final int p4 = iByte & 3;

				alpha |= p1;
				red |= p2;
				green |= p3;
				blue |= p4;

				rgba = alpha << 24 | red << 16 | green << 8 | blue;
				image.setRGB( x, y, rgba );

				++x;

				if ( x >= width )
				{
					x = 0;
					++y;
				}
			}
		}
	};

}
