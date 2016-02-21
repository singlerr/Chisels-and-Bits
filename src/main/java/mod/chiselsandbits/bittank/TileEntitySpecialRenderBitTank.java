package mod.chiselsandbits.bittank;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class TileEntitySpecialRenderBitTank extends FastTESR<TileEntityBitTank>
{

	private static class FluidModelVertex
	{
		final EnumFacing face;
		final double x, yMultiplier, z;
		final double u, v;
		final double uMultiplier, vMultiplier;

		public FluidModelVertex(
				final EnumFacing side,
				final double x,
				final double y,
				final double z,
				final double u1,
				final double v1,
				final double u2,
				final double v2 )
		{
			face = side;
			this.x = x;
			yMultiplier = y;
			this.z = z;

			final double texMultiplier = side.getFrontOffsetY() == 0 ? 8 : 16;

			u = u1 * texMultiplier;
			v = v1 * texMultiplier;
			uMultiplier = u2 * texMultiplier;
			vMultiplier = v2 * texMultiplier;
		}
	};

	FluidModelVertex[] model = new FluidModelVertex[6 * 4];

	public TileEntitySpecialRenderBitTank()
	{
	}

	@Override
	public void renderTileEntityFast(
			final TileEntityBitTank te,
			final double x,
			final double y,
			final double z,
			final float partialTicks,
			final int destroyStage,
			final WorldRenderer worldRenderer )
	{
		if ( destroyStage > 0 )
		{
			return;
		}

		final FluidStack fs = te.getBitsAsFluidStack();
		if ( fs != null )
		{
			final Fluid f = fs.getFluid();
			final int pass = f.getBlock().getBlockLayer() == EnumWorldBlockLayer.TRANSLUCENT ? 1 : 0;

			if ( MinecraftForgeClient.getRenderPass() != pass )
			{
				return;
			}

			model[0] = new FluidModelVertex( EnumFacing.UP, 0, 1, 0, 0, 0, 0, 0 );
			model[1] = new FluidModelVertex( EnumFacing.UP, 1, 1, 0, 1, 0, 0, 0 );
			model[2] = new FluidModelVertex( EnumFacing.UP, 1, 1, 1, 1, 1, 0, 0 );
			model[3] = new FluidModelVertex( EnumFacing.UP, 0, 1, 1, 0, 1, 0, 0 );

			model[4] = new FluidModelVertex( EnumFacing.DOWN, 0, 0, 0, 0, 0, 0, 0 );
			model[5] = new FluidModelVertex( EnumFacing.DOWN, 1, 0, 0, 1, 0, 0, 0 );
			model[6] = new FluidModelVertex( EnumFacing.DOWN, 1, 0, 1, 1, 1, 0, 0 );
			model[7] = new FluidModelVertex( EnumFacing.DOWN, 0, 0, 1, 0, 1, 0, 0 );

			model[8] = new FluidModelVertex( EnumFacing.NORTH, 0, 0, 0, 0, 0, 0, 0 );
			model[9] = new FluidModelVertex( EnumFacing.NORTH, 1, 0, 0, 1, 0, 0, 0 );
			model[10] = new FluidModelVertex( EnumFacing.NORTH, 1, 1, 0, 1, 0, 0, 1 );
			model[11] = new FluidModelVertex( EnumFacing.NORTH, 0, 1, 0, 0, 0, 0, 1 );

			model[12] = new FluidModelVertex( EnumFacing.SOUTH, 0, 0, 1, 0, 0, 0, 0 );
			model[13] = new FluidModelVertex( EnumFacing.SOUTH, 1, 0, 1, 1, 0, 0, 0 );
			model[14] = new FluidModelVertex( EnumFacing.SOUTH, 1, 1, 1, 1, 0, 0, 1 );
			model[15] = new FluidModelVertex( EnumFacing.SOUTH, 0, 1, 1, 0, 0, 0, 1 );

			model[16] = new FluidModelVertex( EnumFacing.EAST, 1, 0, 0, 0, 0, 0, 0 );
			model[17] = new FluidModelVertex( EnumFacing.EAST, 1, 0, 1, 1, 0, 0, 0 );
			model[18] = new FluidModelVertex( EnumFacing.EAST, 1, 1, 1, 1, 0, 0, 1 );
			model[19] = new FluidModelVertex( EnumFacing.EAST, 1, 1, 0, 0, 0, 0, 1 );

			model[20] = new FluidModelVertex( EnumFacing.WEST, 0, 0, 0, 0, 0, 0, 0 );
			model[21] = new FluidModelVertex( EnumFacing.WEST, 0, 0, 1, 1, 0, 0, 0 );
			model[22] = new FluidModelVertex( EnumFacing.WEST, 0, 1, 1, 1, 0, 0, 1 );
			model[23] = new FluidModelVertex( EnumFacing.WEST, 0, 1, 0, 0, 0, 0, 1 );

			final TextureAtlasSprite still = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite( f.getStill().toString() );
			final TextureAtlasSprite flowing = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite( f.getFlowing().toString() );

			final BlockPos pos = te.getPos();

			final int k2 = te.getWorld().getBlockState( pos ).getBlock().getMixedBrightnessForBlock( te.getWorld(), pos );
			final int l2 = k2 >> 16 & 65535;
			final int i3 = k2 & 65535;

			final double fullness = (double) fs.amount / (double) TileEntityBitTank.MAX_CONTENTS;

			worldRenderer.setTranslation( x - pos.getX(), y - pos.getY(), z - pos.getZ() );

			for ( final FluidModelVertex vert : model )
			{
				final EnumFacing face = vert.face;
				final TextureAtlasSprite sprite = face.getFrontOffsetY() != 0 ? still : flowing;

				for ( final VertexFormatElement e : worldRenderer.getVertexFormat().getElements() )
				{
					switch ( e.getUsage() )
					{
						case COLOR:
							worldRenderer.color( 255, 255, 255, 255 );
							break;

						case NORMAL:
							worldRenderer.normal( face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ() );
							break;

						case POSITION:
							final double xx = pos.getX() + vert.x * 0.756 + 0.122;
							final double yy = pos.getY() + vert.yMultiplier * fullness * 0.756 + 0.122;
							final double zz = pos.getZ() + vert.z * 0.756 + 0.122;

							worldRenderer.pos( xx, yy, zz );
							break;

						case UV:
							if ( e.getIndex() == 1 )
							{
								worldRenderer.lightmap( l2, i3 );
							}
							else
							{
								worldRenderer.tex( sprite.getInterpolatedU( vert.u + vert.uMultiplier * fullness ), sprite.getInterpolatedV( 16.0 - ( vert.v + vert.vMultiplier * fullness ) ) );
							}
							break;

						default:
							break;
					}
				}
				worldRenderer.endVertex();
			}

		}
	}

}
