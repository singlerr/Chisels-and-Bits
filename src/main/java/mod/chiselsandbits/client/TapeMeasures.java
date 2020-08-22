package mod.chiselsandbits.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.DeprecationHelper;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.modes.TapeMeasureModes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

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
				final IToolMode chMode,
				final ResourceLocation dimentionid,
				final DyeColor color )
		{
			a = a2;
			b = b2;
			mode = chMode;
			DimensionId = dimentionid;
			this.color = color;
		}

		public final IToolMode        mode;
		public final BitLocation      a;
		public final BitLocation      b;
		public final DyeColor         color;
		public final ResourceLocation DimensionId;
		public       double           distance = 1;

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

		public Vector3d getVecA()
		{
			final double ax = a.blockPos.getX() + bitSize * a.bitX + halfBit;
			final double ay = a.blockPos.getY() + bitSize * a.bitY + halfBit;
			final double az = a.blockPos.getZ() + bitSize * a.bitZ + halfBit;
			return new Vector3d( ax, ay, az );
		}

		public Vector3d getVecB()
		{
			final double bx = b.blockPos.getX() + bitSize * b.bitX + halfBit;
			final double by = b.blockPos.getY() + bitSize * b.bitY + halfBit;
			final double bz = b.blockPos.getZ() + bitSize * b.bitZ + halfBit;
			return new Vector3d( bx, by, bz );
		}

		public void calcDistance(
				final float partialTicks )
		{
			if ( mode == TapeMeasureModes.DISTANCE )
			{
				final Vector3d a = getVecA();
				final Vector3d b = getVecB();
				final PlayerEntity player = ClientSide.instance.getPlayer();
				distance = getLineDistance( a, b, player, partialTicks );
			}
			else
			{
				final PlayerEntity player = ClientSide.instance.getPlayer();
				final Vector3d eyes = player.getEyePosition( partialTicks );
				final AxisAlignedBB box = getBoundingBox();
				if ( box.contains( eyes ) )
				{
					distance = 0.0;
				}
				else
				{
					distance = AABBDistnace( eyes, box );
				}
			}
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
			final IToolMode chMode,
			final ItemStack itemStack )
	{
		final PlayerEntity player = ClientSide.instance.getPlayer();

		if ( a == null || b == null )
		{
			preview = null;
		}
		else
		{
			preview = new Measure( a, b, chMode, getDimension( player ), getColor( itemStack ) );
		}
	}

	public void addMeasure(
			final BitLocation a,
			final BitLocation b,
			final IToolMode chMode,
			final ItemStack itemStack )
	{
		final PlayerEntity player = ClientSide.instance.getPlayer();

		while ( measures.size() > 0 && measures.size() >= ChiselsAndBits.getConfig().getClient().maxTapeMeasures.get() )
		{
			measures.remove( 0 );
		}

		final Measure newMeasure = new Measure( a, b, chMode, getDimension( player ), getColor( itemStack ) );

		if ( ChiselsAndBits.getConfig().getClient().displayMeasuringTapeInChat.get() )
		{
			final AxisAlignedBB box = newMeasure.getBoundingBox();

			final double LenX = box.maxX - box.minX;
			final double LenY = box.maxY - box.minY;
			final double LenZ = box.maxZ - box.minZ;
			final double Len = newMeasure.getVecA().distanceTo( newMeasure.getVecB() );

			final String out = chMode == TapeMeasureModes.DISTANCE ? getSize( Len ) : DeprecationHelper.translateToLocal( "mod.chiselsandbits.tapemeasure.chatmsg", getSize( LenX ), getSize( LenY ), getSize( LenZ ) );

			final StringTextComponent chatMsg = new StringTextComponent( out );

			// NOT 100% Accurate, if anyone wants to try and resolve this, yay
			chatMsg.setStyle( Style.EMPTY.setColor(Color.func_240743_a_(newMeasure.color.getTextColor())) );

			player.sendMessage( chatMsg, Util.DUMMY_UUID );
		}

		measures.add( newMeasure );
	}

	private DyeColor getColor(
			final ItemStack itemStack )
	{
		return ChiselsAndBits.getItems().itemTapeMeasure.getTapeColor( itemStack );
	}

	private ResourceLocation getDimension(
			final PlayerEntity player )
	{
		return player.getEntityWorld().getDimensionKey().getRegistryName();
	}

	public void render(
      final MatrixStack matrixStack, final float partialTicks)
	{
		if ( !measures.isEmpty() || preview != null )
		{
			final PlayerEntity player = ClientSide.instance.getPlayer();

			if ( hasTapeMeasure( player.inventory ) )
			{
				final ArrayList<Measure> sortList = new ArrayList<Measure>( measures.size() + 1 );

				if ( preview != null )
				{
					preview.calcDistance( partialTicks );
					sortList.add( preview );
				}

				for ( final Measure m : measures )
				{
					m.calcDistance( partialTicks );
					sortList.add( m );
				}

				Collections.sort( sortList, new Comparator<Measure>() {

					@Override
					public int compare(
							final Measure a,
							final Measure b )
					{
						return a.distance < b.distance ? 1 : a.distance > b.distance ? -1 : 0;
					}

				} );

				for ( final Measure m : sortList )
				{
					renderMeasure( m, m.distance, matrixStack, partialTicks );
				}
			}
		}
	}

	private boolean hasTapeMeasure(
			final PlayerInventory inventory )
	{
		for ( int x = 0; x < inventory.getSizeInventory(); x++ )
		{
			final ItemStack is = inventory.getStackInSlot( x );
			if ( is != null && is.getItem() == ChiselsAndBits.getItems().itemTapeMeasure )
			{
				return true;
			}
		}

		return false;
	}

	private void renderMeasure(
      final Measure m,
      final double distance,
      final MatrixStack matrixStack,
      final float partialTicks)
	{
		final PlayerEntity player = ClientSide.instance.getPlayer();

		if ( m.DimensionId != getDimension( player ) )
		{
			return;
		}

		final int alpha = getAlphaFromRange( distance );
		if ( alpha < 30 )
		{
			return;
		}

		final double x = player.lastTickPosX + ( player.getPosX() - player.lastTickPosX ) * partialTicks;
		final double y = player.lastTickPosY + ( player.getPosY() - player.lastTickPosY ) * partialTicks;
		final double z = player.lastTickPosZ + ( player.getPosZ() - player.lastTickPosZ ) * partialTicks;

		final int val = m.color.getTextColor();
		final int red = val >> 16 & 0xff;
		final int green = val >> 8 & 0xff;
		final int blue = val & 0xff;
		if ( m.mode == TapeMeasureModes.DISTANCE )
		{
			final Vector3d a = m.getVecA();
			final Vector3d b = m.getVecB();

			RenderHelper.drawLineWithColor( a, b, BlockPos.ZERO, player, partialTicks, false, red, green, blue, alpha, (int) ( alpha / 3.4 ) );

			RenderSystem.disableDepthTest();
			RenderSystem.disableCull();

			final double Len = a.distanceTo( b ) + bitSize;

			renderSize(matrixStack, player, partialTicks, ( a.x + b.x ) * 0.5 - x, ( a.y + b.y ) * 0.5 - y, ( a.z + b.z ) * 0.5 - z, Len, red, green, blue );

			RenderSystem.enableDepthTest();
			RenderSystem.enableCull();
			return;
		}

		final AxisAlignedBB box = m.getBoundingBox();
		RenderHelper.drawSelectionBoundingBoxIfExistsWithColor( box.expand( -0.001, -0.001, -0.001 ), BlockPos.ZERO, player, partialTicks, false, red, green, blue, alpha, (int) ( alpha / 3.4 ) );

		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();

		final double LenX = box.maxX - box.minX;
		final double LenY = box.maxY - box.minY;
		final double LenZ = box.maxZ - box.minZ;

		/**
		 * TODO: Figure out some better logic for which lines to display the
		 * numbers on.
		 **/
		renderSize(matrixStack, player, partialTicks, box.minX - x, ( box.maxY + box.minY ) * 0.5 - y, box.minZ - z, LenY, red, green, blue );
		renderSize(matrixStack, player, partialTicks, ( box.minX + box.maxX ) * 0.5 - x, box.minY - y, box.minZ - z, LenX, red, green, blue );
		renderSize(matrixStack, player, partialTicks, box.minX - x, box.minY - y, ( box.minZ + box.maxZ ) * 0.5 - z, LenZ, red, green, blue );

		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
	}

	private int getAlphaFromRange(
			final double distance )
	{
		if ( distance < 16 )
		{
			return 102;
		}

		return (int) ( 102 - ( distance - 16 ) * 6 );
	}

	private static double AABBDistnace(
			final Vector3d eyes,
			final AxisAlignedBB box )
	{
		// snap eyes into the box...
		final double boxPointX = Math.min( box.maxX, Math.max( box.minX, eyes.x ) );
		final double boxPointY = Math.min( box.maxY, Math.max( box.minY, eyes.y ) );
		final double boxPointZ = Math.min( box.maxZ, Math.max( box.minZ, eyes.z ) );

		// then get the distance to it.
		return Math.sqrt( eyes.squareDistanceTo( boxPointX, boxPointY, boxPointZ ) );
	}

	private static double getLineDistance(
			final Vector3d v,
			final Vector3d w,
			final PlayerEntity player,
			final float partialTicks )
	{
		final Vector3d p = player.getEyePosition( partialTicks );
		final double segmentLength = v.squareDistanceTo( w );

		if ( segmentLength == 0.0 )
		{
			return p.distanceTo( v );
		}

		final double t = Math.max( 0, Math.min( 1, p.subtract( v ).dotProduct( w.subtract( v ) ) / segmentLength ) );
		final Vector3d projection = v.add( w.subtract( v ).scale( t ) );
		return p.distanceTo( projection );
	}

	private void renderSize(
      final MatrixStack matrixStack, final PlayerEntity player,
      final float partialTicks,
      final double x,
      final double y,
      final double z,
      final double len,
      final int red,
      final int green,
      final int blue)
	{
		final double letterSize = 5.0;
		final double zScale = 0.001;

		final FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		final String size = getSize( len );

		matrixStack.push();
		matrixStack.translate( x, y + getScale( len ) * letterSize, z );
		billBoard(matrixStack, player, partialTicks );
		matrixStack.scale( getScale( len ), -getScale( len ), (float) zScale);
		matrixStack.translate( -fontRenderer.getStringWidth( size ) * 0.5, 0, 0 );
		fontRenderer.drawStringWithShadow(matrixStack, size, 0, 0, red << 16 | green << 8 | blue );
		matrixStack.pop();
	}

	private float getScale(
			final double maxLen )
	{
		final double maxFontSize = 0.04;
		final double minFontSize = 0.004;

		final double delta = Math.min( 1.0, maxLen / 4.0 );
		double scale = maxFontSize * delta + minFontSize * ( 1.0 - delta );

		if ( maxLen < 0.25 )
		{
			scale = minFontSize;
		}

		return (float) Math.min( maxFontSize, scale );
	}

	private void billBoard(
	        final MatrixStack matrixStack,
			final PlayerEntity player,
			final float partialTicks )
	{
		final Entity view = Minecraft.getInstance().renderViewEntity;
		if ( view != null )
		{
			final float yaw = view.prevRotationYaw + ( view.rotationYaw - view.prevRotationYaw ) * partialTicks;
			matrixStack.rotate(new Quaternion(Vector3f.YP, 180 + -yaw, true ));

			final float pitch = view.prevRotationPitch + ( view.rotationPitch - view.prevRotationPitch ) * partialTicks;
			matrixStack.rotate(new Quaternion(Vector3f.XP, -pitch, true));
		}
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
