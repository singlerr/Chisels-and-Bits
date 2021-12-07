package mod.chiselsandbits.fabric.platform.client.rendering.ister;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.fabric.platform.client.rendering.FabricRenderingManager;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

public class DelegatingBlockEntityWithoutLevelRendering extends BlockEntityWithoutLevelRenderer
{
    private final BlockEntityWithoutLevelRenderer delegate;

    public DelegatingBlockEntityWithoutLevelRendering(final BlockEntityWithoutLevelRenderer original)
    {
        super(original.blockEntityRenderDispatcher, original.entityModelSet);
        this.delegate = original;
    }

    @Override
    public void renderByItem(
      final ItemStack itemStack, final ItemTransforms.TransformType transformType, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int i, final int j)
    {
        FabricRenderingManager.getInstance()
          .getRenderer(itemStack.getItem())
          .ifPresentOrElse(
            renderer -> renderer.renderByItem(itemStack, transformType, poseStack, multiBufferSource, i, j),
            () -> delegate.renderByItem(itemStack, transformType, poseStack, multiBufferSource, i, j)
          );
    }
}
