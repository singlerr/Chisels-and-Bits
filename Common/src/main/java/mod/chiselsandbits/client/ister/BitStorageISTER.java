package mod.chiselsandbits.client.ister;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.platforms.core.client.rendering.IRenderingManager;
import mod.chiselsandbits.platforms.core.fluid.FluidInformation;
import mod.chiselsandbits.platforms.core.fluid.IFluidManager;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistryManager;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BitStorageISTER extends BlockEntityWithoutLevelRenderer
{
    private static final ItemTransform GUI = new ItemTransform(
      new Vector3f(30,225,0),
      new Vector3f(-0.5f, 0f, 0),
      new Vector3f(0.625f, 0.625f, 0.625f)
    );

    public BitStorageISTER()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
          Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
      final @NotNull ItemStack stack,
      final @NotNull TransformType transformType,
      final PoseStack matrixStack,
      final MultiBufferSource buffer,
      final int combinedLight,
      final int combinedOverlay)
    {

        final BakedModel model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(
          IPlatformRegistryManager.getInstance().getBlockRegistry()
            .getKey(ModBlocks.BIT_STORAGE.get())
          , "facing=east"));

        matrixStack.pushPose();
        GUI.apply(true, matrixStack);

        IRenderingManager.getInstance()
          .renderModel(matrixStack.last(), buffer.getBuffer(RenderType.translucent()), ModBlocks.BIT_STORAGE
                                                                                               .get().defaultBlockState(), model, 1f, 1f, 1f, combinedLight, combinedOverlay);

        matrixStack.popPose();

        final BitStorageBlockEntity blockEntity = new BitStorageBlockEntity(BlockPos.ZERO, ModBlocks.BIT_STORAGE.get().defaultBlockState());
        final Optional<FluidInformation> fluidInformation = IFluidManager.getInstance().get(stack);
        fluidInformation.ifPresent(blockEntity::insertBitsFromFluid);


        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(blockEntity, matrixStack, buffer, combinedLight, combinedOverlay);
    }
}
