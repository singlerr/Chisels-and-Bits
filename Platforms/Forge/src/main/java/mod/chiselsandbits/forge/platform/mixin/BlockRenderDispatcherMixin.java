package mod.chiselsandbits.forge.platform.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.forge.platform.client.rendering.ForgeRenderingManager;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin implements ResourceManagerReloadListener
{
    @Inject(
      method = "renderSingleBlock(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
      at = @At("HEAD"),
      cancellable = true)
    public void onRenderSingleBlock(
      BlockState state,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int x,
      int y,
      CallbackInfo callbackInfo
    ) {
        RenderShape rendershape = state.getRenderShape();
        if (rendershape != RenderShape.INVISIBLE) {
            if (rendershape == RenderShape.ENTITYBLOCK_ANIMATED)
            {
                ItemStack stack = new ItemStack(state.getBlock());
                final Optional<BlockEntityWithoutLevelRenderer> renderer =
                    ForgeRenderingManager.getInstance().getRenderer(stack.getItem());

                if (renderer.isPresent()) {
                    renderer.get().renderByItem(stack, ItemTransforms.TransformType.NONE, poseStack, bufferSource, x, y);
                    callbackInfo.cancel();
                }
            }
        }
    }
}
