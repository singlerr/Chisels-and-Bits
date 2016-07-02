package mod.chiselsandbits.client;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.core.ClientSide;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TapeMeasures
{
	private class Measure
	{
		private static final double bitSize = 1.0 / 16.0;

		public Measure(
				final BitLocation a2,
				final BitLocation b2 )
		{
			a = a2;
			b = b2;
		}

		public final BitLocation a;
		public final BitLocation b;

		public AxisAlignedBB getBoundingBox()
		{
			final double ax = a.blockPos.getX() + bitSize * a.bitX;
			final double ay = a.blockPos.getY() + bitSize * a.bitY;
			final double az = a.blockPos.getZ() + bitSize * a.bitZ;
			final double bx = b.blockPos.getX() + bitSize * b.bitX;
			final double by = b.blockPos.getY() + bitSize * b.bitY;
			final double bz = b.blockPos.getZ() + bitSize * b.bitZ;

			return new AxisAlignedBB(
					Math.min( ax, bx ),
					Math.min( ay, by ),
					Math.min( az, bz ),
					Math.max( ax, bx ) + bitSize,
					Math.max( ay, by ) + bitSize,
					Math.max( az, bz ) + bitSize );
		}
	};

	private final ArrayList<Measure> measures = new ArrayList<Measure>();
	private Measure preview;

	public void clear()
	{
		measures.clear();
	}

	public void setPreviewMeasure(
			final BitLocation a,
			final BitLocation b )
	{
		if ( a == null || b == null )
		{
			preview = null;
		}
		else
		{
			preview = new Measure( a, b );
		}
	}

	public void addMeasure(
			final BitLocation a,
			final BitLocation b )
	{
		measures.add( new Measure( a, b ) );
	}

	public void render(
			final float partialTicks )
	{
		if ( preview != null )
		{
			renderMeasure( preview, partialTicks );
		}

		for ( final Measure m : measures )
		{
			renderMeasure( m, partialTicks );
		}
	}

	private void renderMeasure(
			final Measure m,
			final float partialTicks )
	{
		final AxisAlignedBB box = m.getBoundingBox();
		final EntityPlayer player = ClientSide.instance.getPlayer();
		RenderHelper.drawSelectionBoundingBoxIfExists( box, BlockPos.ORIGIN, player, partialTicks, true );

		final double x = player.lastTickPosX + ( player.posX - player.lastTickPosX ) * partialTicks;
		final double y = player.lastTickPosY + ( player.posY - player.lastTickPosY ) * partialTicks;
		final double z = player.lastTickPosZ + ( player.posZ - player.lastTickPosZ ) * partialTicks;

		GL11.glDisable( GL11.GL_DEPTH_TEST );
		GL11.glDisable( GL11.GL_CULL_FACE );

		final double LenX = box.maxX - box.minX;
		final double LenY = box.maxY - box.minY;
		final double LenZ = box.maxZ - box.minZ;

		final double letterSize = 5.0;
		final double zScale = 0.001;

		GL11.glPushMatrix();
		GL11.glTranslated( box.minX - x, ( box.maxY + box.minY ) * 0.5 - y + getScale( LenY ) * letterSize, box.minZ - z );
		billBoard( player, partialTicks );
		GL11.glScaled( getScale( LenY ), -getScale( LenY ), zScale );
		Minecraft.getMinecraft().fontRendererObj.drawString( getSize( box.maxY - box.minY ), 0, 0, 0xffffff, true );
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated( ( box.minX + box.maxX ) * 0.5 - x, box.minY - y + getScale( LenX ) * letterSize, box.minZ - z );
		billBoard( player, partialTicks );
		GL11.glScaled( getScale( LenX ), -getScale( LenX ), zScale );
		Minecraft.getMinecraft().fontRendererObj.drawString( getSize( box.maxX - box.minX ), 0, 0, 0xffffff, true );
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated( box.minX - x, box.minY - y + getScale( LenZ ) * letterSize, ( box.minZ + box.maxZ ) * 0.5 - z );
		billBoard( player, partialTicks );
		GL11.glScaled( getScale( LenZ ), -getScale( LenZ ), zScale );
		Minecraft.getMinecraft().fontRendererObj.drawString( getSize( box.maxZ - box.minZ ), 0, 0, 0xffffff, true );
		GL11.glPopMatrix();

		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_CULL_FACE );
	}

	private double getScale(
			final double maxLen )
	{
		double scale = 0.04;

		if ( maxLen < 4.0 )
		{
			scale = 0.02;
		}

		if ( maxLen < 1.0 )
		{
			scale = 0.01;
		}

		if ( maxLen < 0.5 )
		{
			scale = 0.005;
		}

		if ( maxLen < 0.25 )
		{
			scale = 0.0025;
		}

		return scale;
	}

	private void billBoard(
			final EntityPlayer player,
			final float partialTicks )
	{
		final Entity view = Minecraft.getMinecraft().getRenderViewEntity();
		final double yaw = view.prevRotationYaw + ( view.rotationYaw - view.prevRotationYaw ) * partialTicks;
		GL11.glRotated( 180 + -yaw, 0, 1, 0 );

		final double pitch = view.prevRotationPitch + ( view.rotationPitch - view.prevRotationPitch ) * partialTicks;
		GL11.glRotated( -pitch, 1, 0, 0 );
	}

	private String getSize(
			final double d )
	{
		final double blocks = Math.floor( d );
		final double bits = d - blocks;

		final StringBuilder b = new StringBuilder();

		if ( blocks > 0 )
		{
			b.append( (int) blocks ).append( "B" );
		}

		if ( bits > 0 )
		{
			if ( b.length() > 0 )
			{
				b.append( " " );
			}
			b.append( (int) ( bits * 16 ) ).append( "b" );
		}

		return b.toString();
	}

}
