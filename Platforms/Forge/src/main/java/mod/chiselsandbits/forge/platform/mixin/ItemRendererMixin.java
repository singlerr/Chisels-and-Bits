package mod.chiselsandbits.forge.platform.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.chiselsandbits.forge.platform.client.rendering.ForgeRenderingManager;
import net.minecraft.client.Option;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = ItemRenderer.class)
public class ItemRendererMixin
{

    @Shadow @Final private ItemModelShaper itemModelShaper;

    @Inject(
      method = "render",
      at = @At("HEAD"),
      cancellable = true
    )
    public void render(ItemStack p_115144_, ItemTransforms.TransformType p_115145_, boolean p_115146_, PoseStack p_115147_, MultiBufferSource p_115148_, int p_115149_, int p_115150_, BakedModel p_115151_, CallbackInfo callbackInfo) {
        if (p_115144_.isEmpty())
        {
            return;
        }

        final Optional<BlockEntityWithoutLevelRenderer> renderer = ForgeRenderingManager.getInstance().getRenderer(p_115144_.getItem());
        if (renderer.isEmpty() || !p_115151_.isCustomRenderer()) {
            return;
        }

        p_115147_.pushPose();
        p_115147_.translate(-0.5D, -0.5D, -0.5D);
        renderer.get().renderByItem(
          p_115144_, p_115145_, p_115147_, p_115148_, p_115149_, p_115150_
        );

        p_115147_.popPose();

        callbackInfo.cancel();
    }
}
