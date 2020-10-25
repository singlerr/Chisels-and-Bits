package mod.chiselsandbits.bittank;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.chiselsandbits.utils.FluidCuboidHelper;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public class TileEntitySpecialRenderBitTank extends TileEntityRenderer<TileEntityBitTank>
{

    public TileEntitySpecialRenderBitTank(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
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
        if (fluidStack != null)
        {
            RenderType.getBlockRenderTypes().forEach(renderType -> {
                if (!RenderTypeLookup.canRenderInLayer(fluidStack.getFluid().getDefaultState(), renderType))
                    return;

                final IVertexBuilder builder = buffer.getBuffer(renderType);

                final float fullness = (float) fluidStack.getAmount() / (float) TileEntityBitTank.MAX_CONTENTS;

                FluidCuboidHelper.renderScaledFluidCuboid(
                  fluidStack,
                  matrixStackIn,
                  builder,
                  combinedLightIn,
                  3, 3, 3,
                  13, 13 * fullness, 13
                );
            });
        }
    }
}
