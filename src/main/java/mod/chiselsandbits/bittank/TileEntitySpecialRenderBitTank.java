package mod.chiselsandbits.bittank;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.chiselsandbits.utils.FluidCuboidHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
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

                if (renderType == RenderType.getTranslucent() && Minecraft.isFabulousGraphicsEnabled())
                    renderType = Atlases.getTranslucentCullBlockType();

                final IVertexBuilder builder = buffer.getBuffer(renderType);

                final float fullness = (float) fluidStack.getAmount() / (float) TileEntityBitTank.MAX_CONTENTS;

                FluidCuboidHelper.renderScaledFluidCuboid(
                  fluidStack,
                  matrixStackIn,
                  builder,
                  combinedLightIn,
                  combinedOverlayIn,
                  1, 1, 1,
                  15, 15 * fullness, 15
                );
            });
        }
    }
}
