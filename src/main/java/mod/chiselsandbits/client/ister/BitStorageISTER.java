package mod.chiselsandbits.client.ister;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;

public class BitStorageISTER extends BlockEntityWithoutLevelRenderer
{
    public BitStorageISTER()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
          Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
      final @NotNull ItemStack stack,
      final ItemTransforms.@NotNull TransformType transformType,
      final PoseStack matrixStack,
      final MultiBufferSource buffer,
      final int combinedLight,
      final int combinedOverlay)
    {

        final BakedModel model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(ModBlocks.BIT_STORAGE.getId(), "facing=east"));

        Minecraft.getInstance()
          .getBlockRenderer()
          .getModelRenderer()
          .renderModel(matrixStack.last(), buffer.getBuffer(RenderType.translucent()), ModBlocks.BIT_STORAGE
                                                                                               .get().defaultBlockState(), model, 1f, 1f, 1f, combinedLight, combinedOverlay,
            EmptyModelData.INSTANCE);

        final BitStorageBlockEntity tileEntity = new BitStorageBlockEntity(BlockPos.ZERO, ModBlocks.BIT_STORAGE.get().defaultBlockState());
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

        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(tileEntity, matrixStack, buffer, combinedLight, combinedOverlay);
    }
}
