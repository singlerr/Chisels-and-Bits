package mod.chiselsandbits.client.ister;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
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
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;

public class BitStorageISTER extends ItemStackTileEntityRenderer
{
    @Override
    public void renderByItem(
      final @NotNull ItemStack stack,
      final ItemCameraTransforms.@NotNull TransformType transformType,
      final MatrixStack matrixStack,
      final IRenderTypeBuffer buffer,
      final int combinedLight,
      final int combinedOverlay)
    {

        final IBakedModel model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(ModBlocks.BIT_STORAGE.getId(), "facing=east"));

        Minecraft.getInstance()
          .getBlockRenderer()
          .getModelRenderer()
          .renderModel(matrixStack.last(), buffer.getBuffer(RenderType.translucent()), ModBlocks.BIT_STORAGE
                                                                                               .get().defaultBlockState(), model, 1f, 1f, 1f, combinedLight, combinedOverlay,
            EmptyModelData.INSTANCE);

        final BitStorageBlockEntity tileEntity = new BitStorageBlockEntity();
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
