package mod.chiselsandbits.bittank;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.chiselsandbits.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ItemStackSpecialRendererBitTank extends ItemStackTileEntityRenderer
{

    @Override
    public void func_239207_a_(
      final ItemStack stack,
      final ItemCameraTransforms.TransformType p_239207_2_,
      final MatrixStack matrixStack,
      final IRenderTypeBuffer buffer,
      final int combinedLight,
      final int combinedOverlay)
    {

        final IBakedModel model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(ModBlocks.BIT_TANK_BLOCK.getId(), "facing=east"));

        Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(matrixStack.getLast(), buffer.getBuffer(RenderTypeLookup.func_239220_a_(ModBlocks.BIT_TANK_BLOCK.get().getDefaultState(), false)), ModBlocks.BIT_TANK_BLOCK.get().getDefaultState(), model, 1f,1f,1f, combinedLight, combinedOverlay,
          EmptyModelData.INSTANCE);

        final TileEntityBitTank tileEntity = new TileEntityBitTank();
        tileEntity
          .getCapability(
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
          )
          .ifPresent(t -> t
                            .fill(
                              stack.getCapability(
                                CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
                              )
                                .map(s -> s
                                            .drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE)
                                )
                                .orElse(FluidStack.EMPTY),
                              IFluidHandler.FluidAction.EXECUTE
                            )
          );

        TileEntityRendererDispatcher.instance.renderItem(tileEntity, matrixStack, buffer, combinedLight, combinedOverlay);
    }
}
