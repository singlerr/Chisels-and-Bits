package mod.chiselsandbits.client;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.modes.TapeMeasureModes;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TapeMeasures
{
	private static final double blockSize = 1.0;
	private static final double bitSize = 1.0 / 16.0;
	private static final double halfBit = bitSize / 2.0f;

	private class Measure
	{
		public Measure(
				final BitLocation a2,
				final BitLocation b2,
				final IToolMode chMode )
		{
			a = a2;
			b = b2;
			mode = chMode;
		}

		public final IToolMode mode;
		public final BitLocation a;
		public final BitLocation b;

		public AxisAlignedBB getBoundingBox()
		{
			if ( mode == TapeMeasureModes.BLOCK )
			{
				final double ax = a.blockPos.getX();
				final double ay = a.blockPos.getY();
				final double az = a.blockPos.getZ();
				final double bx = b.blockPos.getX();
				final double by = b.blockPos.getY();
				final double bz = b.blockPos.getZ();

				return new AxisAlignedBB(
						Math.min( ax, bx ),
						Math.min( ay, by ),
						Math.min( az, bz ),
						Math.max( ax, bx ) + blockSize,
						Math.max( ay, by ) + blockSize,
						Math.max( az, bz ) + blockSize );
			}

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

		public Vec3d getVecA()
		{
			final double ax = a.blockPos.getX() + bitSize * a.bitX + halfBit;
			final double ay = a.blockPos.getY() + bitSize * a.bitY + halfBit;
			final double az = a.blockPos.getZ() + bitSize * a.bitZ + halfBit;
			return new Vec3d( ax, ay, az );
		}

		public Vec3d getVecB()
		{
			final double bx = b.blockPos.getX() + bitSize * b.bitX + halfBit;
			final double by = b.blockPos.getY() + bitSize * b.bitY + halfBit;
			final double bz = b.blockPos.getZ() + bitSize * b.bitZ + halfBit;
			return new Vec3d( bx, by, bz );
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
			final BitLocation b,
			final IToolMode chMode )
	{
		if ( a == null || b == null )
		{
			preview = null;
		}
		else
		{
			preview = new Measure( a, b, chMode );
		}
	}

	public void addMeasure(
			final BitLocation a,
			final BitLocation b,
			final IToolMode chMode )
	{
		if ( measures.size() > 0 && measures.size() >= ChiselsAndBits.getConfig().maxTapeMeasures )
		{
			measures.remove( 0 );
		}

		measures.add( new Measure( a, b, chMode ) );
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
		final EntityPlayer player = ClientSide.instance.getPlayer();

		final double x = player.lastTickPosX + ( player.posX - player.lastTickPosX ) * partialTicks;
		final double y = player.lastTickPosY + ( player.posY - player.lastTickPosY ) * partialTicks;
		final double z = player.lastTickPosZ + ( player.posZ - player.lastTickPosZ ) * partialTicks;

		final double letterSize = 5.0;
		final double zScale = 0.001;

		if ( m.mode == TapeMeasureModes.DISTANCE )
		{
			final Vec3d a = m.getVecA();
			final Vec3d b = m.getVecB();

			RenderHelper.drawLineWithColor( a, b, BlockPos.ORIGIN, player, partialTicks, false, 255, 255, 255, 102, 30 );

			GL11.glDisable( GL11.GL_DEPTH_TEST );
			GL11.glDisable( GL11.GL_CULL_FACE );

			final double Len = a.distanceTo( b ) + bitSize;

			GL11.glPushMatrix();
			GL11.glTranslated( ( a.xCoord + b.xCoord ) * 0.5 - x, ( a.yCoord + b.yCoord ) * 0.5 - y + getScale( Len ) * letterSize, ( a.zCoord + b.zCoord ) * 0.5 - z );
			billBoard( player, partialTicks );
			GL11.glScaled( getScale( Len ), -getScale( Len ), zScale );
			Minecraft.getMinecraft().fontRendererObj.drawString( getSize( Len ), 0, 0, 0xffffff, true );
			GL11.glPopMatrix();

			GL11.glEnable( GL11.GL_DEPTH_TEST );
			GL11.glEnable( GL11.GL_CULL_FACE );
			return;
		}

		final AxisAlignedBB box = m.getBoundingBox();
		RenderHelper.drawSelectionBoundingBoxIfExistsWithColor( box.expand( -0.001, -0.001, -0.001 ), BlockPos.ORIGIN, player, partialTicks, false, 255, 255, 255, 102, 30 );

		GL11.glDisable( GL11.GL_DEPTH_TEST );
		GL11.glDisable( GL11.GL_CULL_FACE );

		final double LenX = box.maxX - box.minX;
		final double LenY = box.maxY - box.minY;
		final double LenZ = box.maxZ - box.minZ;

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
		final double delta = Math.min( 1.0, maxLen / 2.0 );
		double scale = 0.04 * delta + 0.0025 * ( 1.0 - delta );

		if ( maxLen < 0.25 )
		{
			scale = 0.0025;
		}

		return Math.min( 0.04, scale );
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
			b.append( (int) blocks ).append( "m" );
		}

		if ( bits * 16 > 0.9999 )
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
