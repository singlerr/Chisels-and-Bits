package mod.chiselsandbits.fabric.mixin.platform.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.platforms.core.client.models.ITransformAwareBakedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererTransformAwareBakedModelMixin
{

    @Inject(
      method = "render",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/resources/model/BakedModel;getTransforms()Lnet/minecraft/client/renderer/block/model/ItemTransforms;"
      )
    )
    public void handleTransformAwareModel(

      final ItemStack itemStack,
      final ItemTransforms.TransformType transformType,
      final boolean bl,
      final PoseStack poseStack,
      final MultiBufferSource multiBufferSource,
      final int i,
      final int j,
      final BakedModel bakedModel,
      final CallbackInfo ci) {
        if (bakedModel instanceof ITransformAwareBakedModel) {
            ((ITransformAwareBakedModel) bakedModel).handlePerspective(transformType, poseStack);
        }
    }
}
