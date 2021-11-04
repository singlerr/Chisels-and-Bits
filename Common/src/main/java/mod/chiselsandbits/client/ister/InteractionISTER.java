package mod.chiselsandbits.client.ister;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.item.interactable.IInteractableItem;
import mod.chiselsandbits.client.events.TickHandler;
import mod.chiselsandbits.client.model.baked.interactable.InteractableBakedItemModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;

/**
 * This class animates an interaction between the items in the two hands of the players.
 *
 * Cloned from: Creators-of-Create: https://github.com/Creators-of-Create/Create/blob/mc1.16/dev/src/main/java/com/simibubi/create/content/curiosities/tools/SandPaperItemRenderer.java
 * Modified some behaviour and fields to target the general use better, but functionally the same.
 */
public class InteractionISTER extends BlockEntityWithoutLevelRenderer
{
    public InteractionISTER()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
          Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
      final @NotNull ItemStack stack,
      final @NotNull TransformType transformType,
      final @NotNull PoseStack matrixStack,
      final @NotNull MultiBufferSource buffer,
      final int combinedLight,
      final int combinedOverlay)
    {
        if (!(stack.getItem() instanceof IInteractableItem)) {
            return;
        }
        final IInteractableItem item = (IInteractableItem) stack.getItem();

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        LocalPlayer player = Minecraft.getInstance().player;
        BakedModel mainModel = itemRenderer.getModel(stack, Minecraft.getInstance().level, null, 0);
        if (!(mainModel instanceof InteractableBakedItemModel))
        {
            return;
        }
        BakedModel innerModel = ((InteractableBakedItemModel) mainModel).getInnerModel();

        float partialTicks = Minecraft.getInstance().getFrameTime();

        boolean leftHand = transformType == TransformType.FIRST_PERSON_LEFT_HAND;
        boolean firstPerson = leftHand || transformType == TransformType.FIRST_PERSON_RIGHT_HAND;

        matrixStack.pushPose();
        matrixStack.translate(.5f, .5f, .5f);

        boolean jeiMode = item.isRunningASimulatedInteraction(stack);

        if (item.isInteracting(stack)) {
            matrixStack.pushPose();

            if (transformType == TransformType.GUI) {
                matrixStack.translate(0.0F, .2f, 1.0F);
                matrixStack.scale(.75f, .75f, .75f);
            } else {
                int modifier = leftHand ? -1 : 1;
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(modifier * 40));
            }

            // Reverse bobbing
            float time = 0;
            if (player != null)
            {
                time = (float) (!jeiMode ? player.getUseItemRemainingTicks()
                                        : (-TickHandler.getNonePausedTicks()) % stack.getUseDuration()) - partialTicks + 1.0F;
            }
            if (time / (float) stack.getUseDuration() < 0.8F) {
                float bobbing = -Mth.abs(Mth.cos(time / item.getBobbingTickCount() * (float) Math.PI) * 0.1F);

                if (transformType == TransformType.GUI)
                    matrixStack.translate(bobbing, bobbing, 0.0F);
                else
                    matrixStack.translate(0.0f, bobbing, 0.0F);
            }

            itemRenderer.render(stack, TransformType.NONE, false, matrixStack, buffer, combinedLight, combinedOverlay, innerModel);

            matrixStack.popPose();
        }

        if (firstPerson && player != null) {
            int itemInUseCount = player.getUseItemRemainingTicks();
            if (itemInUseCount > 0) {
                int modifier = leftHand ? -1 : 1;
                matrixStack.translate(modifier * .5f, 0, -.25f);
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(modifier * 40));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(modifier * 10));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(modifier * 90));
            }
        }

        if (!item.isInteracting(stack))
        {
            itemRenderer.render(stack, TransformType.NONE, false, matrixStack, buffer, combinedLight, combinedOverlay, innerModel);
        }
        else
        {
            final ItemStack target = item.getInteractionTarget(stack);
            itemRenderer.renderStatic(target, TransformType.NONE, combinedLight, combinedOverlay, matrixStack, buffer, 0);
        }

        matrixStack.popPose();
    }
}
