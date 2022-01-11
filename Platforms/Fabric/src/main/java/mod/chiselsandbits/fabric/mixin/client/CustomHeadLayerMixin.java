package mod.chiselsandbits.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.item.MonocleItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin
{

    @Inject(
      method = "render*",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/layers/CustomHeadLayer;translateToHead(Lcom/mojang/blaze3d/vertex/PoseStack;Z)V"
      ),
      cancellable = true
    )
    public void onRenderLayer(PoseStack poseStack, MultiBufferSource multiBufferSource, int p_116733_, LivingEntity entity, float p_116735_, float p_116736_, float p_116737_, float p_116738_, float p_116739_, float p_116740_, final CallbackInfo callbackInfo) {
        ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (itemstack.getItem() instanceof MonocleItem) {
            poseStack.popPose();
            callbackInfo.cancel();
        }
    }
}
