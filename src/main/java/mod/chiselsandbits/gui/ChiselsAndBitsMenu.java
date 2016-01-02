package mod.chiselsandbits.gui;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Stopwatch;

import mod.chiselsandbits.ChiselMode;
import mod.chiselsandbits.ClientSide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class ChiselsAndBitsMenu extends GuiScreen
{

	public static final ChiselsAndBitsMenu instance = new ChiselsAndBitsMenu();

	private final float TIME_SCALE = 0.01f;
	private float visibility = 0.0f;
	private Stopwatch lastChange = Stopwatch.createStarted();
	public ChiselMode switchTo = null;

	private float clampVis(
			final float f )
	{
		return Math.max( 0.0f, Math.min( 1.0f, f ) );
	}

	public void raiseVisibility()
	{
		visibility = clampVis( visibility + lastChange.elapsed( TimeUnit.MILLISECONDS ) * TIME_SCALE );
		lastChange = Stopwatch.createStarted();
	}

	public void decreaseVisibility()
	{
		visibility = clampVis( visibility - lastChange.elapsed( TimeUnit.MILLISECONDS ) * TIME_SCALE );
		lastChange = Stopwatch.createStarted();
	}

	public boolean isVisible()
	{
		return visibility > 0.001;
	}

	public void configure(
			final int scaledWidth,
			final int scaledHeight )
	{
		mc = Minecraft.getMinecraft();
		fontRendererObj = mc.fontRendererObj;
		width = scaledWidth;
		height = scaledHeight;
	}

	private static class MenuRegion
	{

		public final ChiselMode mode;
		public double x1, x2;
		public double y1, y2;
		public boolean highlighted;

		public MenuRegion(
				final ChiselMode mode )
		{
			this.mode = mode;
		}

	};

	@Override
	public void drawScreen(
			final int mouseX,
			final int mouseY,
			final float partialTicks )
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate( 0.0F, 0.0F, 200.0F );

		final int start = (int) ( visibility * 98 ) << 24;
		final int end = (int) ( visibility * 128 ) << 24;

		drawGradientRect( 0, 0, width, height, start, end );

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate( 770, 771, 1, 0 );
		GlStateManager.shadeModel( 7425 );
		final Tessellator tessellator = Tessellator.getInstance();
		final WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.begin( GL11.GL_QUADS,
				DefaultVertexFormats.POSITION_COLOR );

		final double vecX = mouseX - width / 2;
		final double vecY = mouseY - height / 2;
		double radians = Math.atan2( vecY, vecX );
		final double length = Math.sqrt( vecX * vecX + vecY * vecY );

		final double m1 = 20;
		final double m2 = 50;
		final double m3 = 65;
		final double quarterCircle = Math.PI / 2.0;

		if ( radians < -quarterCircle )
		{
			radians = radians + Math.PI * 2;
		}

		final ArrayList<MenuRegion> modes = new ArrayList<MenuRegion>();

		final EnumSet<ChiselMode> used = EnumSet.noneOf( ChiselMode.class );
		final ChiselMode[] orderedModes = { ChiselMode.SINGLE, ChiselMode.LINE, ChiselMode.PLANE, ChiselMode.CONNECTED_PLANE, ChiselMode.DRAWN_REGION };

		for ( final ChiselMode mode : orderedModes )
		{
			if ( !mode.isDisabled )
			{
				modes.add( new MenuRegion( mode ) );
				used.add( mode );
			}
		}

		for ( final ChiselMode mode : ChiselMode.values() )
		{
			if ( !mode.isDisabled && !used.contains( mode ) )
			{
				modes.add( new MenuRegion( mode ) );
			}
		}

		final double middle_x = width / 2;
		final double middle_y = height / 2;
		switchTo = null;

		if ( !modes.isEmpty() )
		{
			final int totalModes = modes.size();
			int currentMode = 0;
			final double fragment = Math.PI * 0.005;
			final double fragment2 = Math.PI * 0.0025;
			final double perObject = 2.0 * Math.PI / totalModes;

			for ( final MenuRegion mr : modes )
			{
				final double begin_rad = currentMode * perObject - quarterCircle;
				final double end_rad = ( currentMode + 1 ) * perObject - quarterCircle;

				mr.x1 = Math.cos( begin_rad );
				mr.x2 = Math.cos( end_rad );
				mr.y1 = Math.sin( begin_rad );
				mr.y2 = Math.sin( end_rad );

				final double x1m1 = Math.cos( begin_rad + fragment ) * m1;
				final double x2m1 = Math.cos( end_rad - fragment ) * m1;
				final double y1m1 = Math.sin( begin_rad + fragment ) * m1;
				final double y2m1 = Math.sin( end_rad - fragment ) * m1;

				final double x1m2 = Math.cos( begin_rad + fragment2 ) * m2;
				final double x2m2 = Math.cos( end_rad - fragment2 ) * m2;
				final double y1m2 = Math.sin( begin_rad + fragment2 ) * m2;
				final double y2m2 = Math.sin( end_rad - fragment2 ) * m2;

				final float a = 0.5f;
				float f = 0f;

				if ( begin_rad <= radians && radians <= end_rad && m1 < length && length <= m2 )
				{
					f = 1;
					mr.highlighted = true;
					switchTo = mr.mode;
				}

				worldrenderer.pos( middle_x + x1m1, middle_y + y1m1,
						zLevel ).color( f, f, f, a ).endVertex();
				worldrenderer.pos( middle_x + x2m1, middle_y + y2m1,
						zLevel ).color( f, f, f, a ).endVertex();
				worldrenderer.pos( middle_x + x2m2, middle_y + y2m2,
						zLevel ).color( f, f, f, a ).endVertex();
				worldrenderer.pos( middle_x + x1m2, middle_y + y1m2,
						zLevel ).color( f, f, f, a ).endVertex();

				currentMode++;
			}
		}

		tessellator.draw();

		GlStateManager.shadeModel( 7424 );

		GlStateManager.translate( 0.0F, 0.0F, 5.0F );
		GlStateManager.enableTexture2D();
		GlStateManager.color( 1, 1, 1, 1.0f );
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		Minecraft.getMinecraft().getTextureManager().bindTexture( TextureMap.locationBlocksTexture );

		worldrenderer.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR );
		for ( final MenuRegion mr : modes )
		{
			final double x = ( mr.x1 + mr.x2 ) * 0.5 * ( m2 * 0.6 + 0.4 * m1 );
			final double y = ( mr.y1 + mr.y2 ) * 0.5 * ( m2 * 0.6 + 0.4 * m1 );

			final SpriteIconPositioning sip = ClientSide.instance.getIconForMode( mr.mode );

			final double scalex = 15 * sip.width * 0.5;
			final double scaley = 15 * sip.height * 0.5;
			final double x1 = x - scalex;
			final double x2 = x + scalex;
			final double y1 = y - scaley;
			final double y2 = y + scaley;

			final TextureAtlasSprite sprite = sip.sprite;

			final float f = 1.0f;
			final float a = 1.0f;

			final double u1 = sip.left * 16.0;
			final double u2 = ( sip.left + sip.width ) * 16.0;
			final double v1 = sip.top * 16.0;
			final double v2 = ( sip.top + sip.height ) * 16.0;

			worldrenderer.pos( middle_x + x1, middle_y + y1, zLevel ).tex( sprite.getInterpolatedU( u1 ), sprite.getInterpolatedV( v1 ) ).color( f, f, f, a ).endVertex();
			worldrenderer.pos( middle_x + x1, middle_y + y2, zLevel ).tex( sprite.getInterpolatedU( u1 ), sprite.getInterpolatedV( v2 ) ).color( f, f, f, a ).endVertex();
			worldrenderer.pos( middle_x + x2, middle_y + y2, zLevel ).tex( sprite.getInterpolatedU( u2 ), sprite.getInterpolatedV( v2 ) ).color( f, f, f, a ).endVertex();
			worldrenderer.pos( middle_x + x2, middle_y + y1, zLevel ).tex( sprite.getInterpolatedU( u2 ), sprite.getInterpolatedV( v1 ) ).color( f, f, f, a ).endVertex();
		}

		tessellator.draw();

		for ( final MenuRegion mr : modes )
		{
			if ( mr.highlighted )
			{
				final double x = ( mr.x1 + mr.x2 ) * 0.5;
				final double y = ( mr.y1 + mr.y2 ) * 0.5;

				int fixed_x = (int) ( x * m3 );
				final int fixed_y = (int) ( y * m3 );
				final String text = mr.mode.string.getLocal();

				if ( x <= -0.2 )
				{
					fixed_x -= fontRendererObj.getStringWidth( text );
				}
				else if ( -0.2 <= x && x <= 0.2 )
				{
					fixed_x -= fontRendererObj.getStringWidth( text ) / 2;
				}

				fontRendererObj.drawString( text, (int) middle_x + fixed_x, (int) middle_y + fixed_y, 0xffffffff );
			}
		}
		GlStateManager.popMatrix();
	}

}
