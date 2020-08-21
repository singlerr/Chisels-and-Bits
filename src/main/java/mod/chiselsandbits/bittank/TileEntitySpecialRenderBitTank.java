package mod.chiselsandbits.bittank;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;

public class TileEntitySpecialRenderBitTank extends TileEntityRenderer<TileEntityBitTank>
{

	private final FluidModelVertex[] model = new FluidModelVertex[6 * 4];

	public TileEntitySpecialRenderBitTank(TileEntityRendererDispatcher dispatcher)
	{
	    super(dispatcher);

		model[0] = new FluidModelVertex( Direction.UP, 0, 1, 0, 0, 0, 0, 0 );
		model[1] = new FluidModelVertex( Direction.UP, 1, 1, 0, 1, 0, 0, 0 );
		model[2] = new FluidModelVertex( Direction.UP, 1, 1, 1, 1, 1, 0, 0 );
		model[3] = new FluidModelVertex( Direction.UP, 0, 1, 1, 0, 1, 0, 0 );

		model[4] = new FluidModelVertex( Direction.DOWN, 0, 0, 0, 0, 0, 0, 0 );
		model[5] = new FluidModelVertex( Direction.DOWN, 1, 0, 0, 1, 0, 0, 0 );
		model[6] = new FluidModelVertex( Direction.DOWN, 1, 0, 1, 1, 1, 0, 0 );
		model[7] = new FluidModelVertex( Direction.DOWN, 0, 0, 1, 0, 1, 0, 0 );

		model[8] = new FluidModelVertex( Direction.NORTH, 0, 0, 0, 0, 0, 0, 0 );
		model[9] = new FluidModelVertex( Direction.NORTH, 1, 0, 0, 1, 0, 0, 0 );
		model[10] = new FluidModelVertex( Direction.NORTH, 1, 1, 0, 1, 0, 0, 1 );
		model[11] = new FluidModelVertex( Direction.NORTH, 0, 1, 0, 0, 0, 0, 1 );

		model[12] = new FluidModelVertex( Direction.SOUTH, 0, 0, 1, 0, 0, 0, 0 );
		model[13] = new FluidModelVertex( Direction.SOUTH, 1, 0, 1, 1, 0, 0, 0 );
		model[14] = new FluidModelVertex( Direction.SOUTH, 1, 1, 1, 1, 0, 0, 1 );
		model[15] = new FluidModelVertex( Direction.SOUTH, 0, 1, 1, 0, 0, 0, 1 );

		model[16] = new FluidModelVertex( Direction.EAST, 1, 0, 0, 0, 0, 0, 0 );
		model[17] = new FluidModelVertex( Direction.EAST, 1, 0, 1, 1, 0, 0, 0 );
		model[18] = new FluidModelVertex( Direction.EAST, 1, 1, 1, 1, 0, 0, 1 );
		model[19] = new FluidModelVertex( Direction.EAST, 1, 1, 0, 0, 0, 0, 1 );

		model[20] = new FluidModelVertex( Direction.WEST, 0, 0, 0, 0, 0, 0, 0 );
		model[21] = new FluidModelVertex( Direction.WEST, 0, 0, 1, 1, 0, 0, 0 );
		model[22] = new FluidModelVertex( Direction.WEST, 0, 1, 1, 1, 0, 0, 1 );
		model[23] = new FluidModelVertex( Direction.WEST, 0, 1, 0, 0, 0, 0, 1 );
	}

    @Override
    public void render(
      final TileEntityBitTank te,
      final float partialTicks,
      final MatrixStack matrixStackIn,
      final IRenderTypeBuffer buffer,
      final int combinedLightIn,
      final int combinedOverlayIn)
    {
        final FluidStack fluidStack = te.getBitsAsFluidStack();
        if ( fluidStack != null )
        {
            final Fluid fluid = fluidStack.getFluid();

            final IVertexBuilder builder = buffer.getBuffer(MinecraftForgeClient.getRenderLayer());
            final TextureAtlasSprite still = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply( fluid.getAttributes().getStillTexture() );
            final TextureAtlasSprite flowing = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply( fluid.getAttributes().getFlowingTexture() );

            final BlockPos pos = te.getPos();

            final int mixedBrightness = combinedLightIn;
            final int skyLight = mixedBrightness >> 16 & 65535;
            final int blockLight = mixedBrightness & 65535;

            final double fullness = (double) fluidStack.getAmount() / (double) TileEntityBitTank.MAX_CONTENTS;

            final int rgbaColor = fluid.getAttributes().getColor();
            final int rColor = rgbaColor >> 16 & 0xff;
            final int gColor = rgbaColor >> 8 & 0xff;
            final int bColor = rgbaColor & 0xff;
            final int aColor = rgbaColor >> 24 & 0xff;

            for ( final FluidModelVertex vert : model )
            {
                final Direction face = vert.face;
                final TextureAtlasSprite sprite = face.getYOffset() != 0 ? still : flowing;

                for ( final VertexFormatElement e : MinecraftForgeClient.getRenderLayer().getVertexFormat().getElements() )
                {
                    switch ( e.getUsage() )
                    {
                        case COLOR:
                            builder.color( rColor, gColor, bColor, aColor );
                            break;

                        case NORMAL:
                            builder.normal( face.getXOffset(), face.getYOffset(), face.getZOffset() );
                            break;

                        case POSITION:
                            final double vertX = pos.getX() + vert.x * 0.756 + 0.122;
                            final double vertY = pos.getY() + vert.yMultiplier * fullness * 0.756 + 0.122;
                            final double vertZ = pos.getZ() + vert.z * 0.756 + 0.122;

                            builder.pos( vertX, vertY, vertZ );
                            break;

                        case UV:
                            if ( e.getIndex() == 1 )
                            {
                                builder.lightmap( skyLight, blockLight );
                            }
                            else
                            {
                                builder.tex( sprite.getInterpolatedU( vert.u + vert.uMultiplier * fullness ), sprite.getInterpolatedV( 16.0 - ( vert.v + vert.vMultiplier * fullness ) ) );
                            }
                            break;

                        default:
                            break;
                    }
                }
                builder.endVertex();
            }
        }
    }
}
