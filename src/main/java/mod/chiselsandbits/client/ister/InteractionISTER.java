package mod.chiselsandbits.client.ister;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.chiselsandbits.api.item.interactable.IInteractableItem;
import mod.chiselsandbits.client.events.TickHandler;
import mod.chiselsandbits.client.model.baked.interactable.InteractableBakedItemModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import org.jetbrains.annotations.NotNull;

/**
 * This class animates an interaction between the items in the two hands of the players.
 *
 * Cloned from: Creators-of-Create: https://github.com/Creators-of-Create/Create/blob/mc1.16/dev/src/main/java/com/simibubi/create/content/curiosities/tools/SandPaperItemRenderer.java
 * Modified some behaviour and fields to target the general use better, but functionally the same.
 */
public class InteractionISTER extends ItemStackTileEntityRenderer
{
    @Override
    public void func_239207_a_(
      final @NotNull ItemStack stack,
      final ItemCameraTransforms.@NotNull TransformType transformType,
      final @NotNull MatrixStack matrixStack,
      final @NotNull IRenderTypeBuffer buffer,
      final int combinedLight,
      final int combinedOverlay)
    {
        if (!(stack.getItem() instanceof IInteractableItem)) {
            return;
        }
        final IInteractableItem item = (IInteractableItem) stack.getItem();

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ClientPlayerEntity player = Minecraft.getInstance().player;
        IBakedModel mainModel = itemRenderer.getItemModelWithOverrides(stack, Minecraft.getInstance().world, null);
        if (!(mainModel instanceof InteractableBakedItemModel))
        {
            return;
        }
        IBakedModel innerModel = ((InteractableBakedItemModel) mainModel).getInnerModel();

        float partialTicks = Minecraft.getInstance().getRenderPartialTicks();

        boolean leftHand = transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
        boolean firstPerson = leftHand || transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND;

        matrixStack.push();
        matrixStack.translate(.5f, .5f, .5f);

        boolean jeiMode = item.isRunningASimulatedInteraction(stack);

        if (item.isInteracting(stack)) {
            matrixStack.push();

            if (transformType == ItemCameraTransforms.TransformType.GUI) {
                matrixStack.translate(0.0F, .2f, 1.0F);
                matrixStack.scale(.75f, .75f, .75f);
            } else {
                int modifier = leftHand ? -1 : 1;
                matrixStack.rotate(Vector3f.YP.rotationDegrees(modifier * 40));
            }

            // Reverse bobbing
            float time = 0;
            if (player != null)
            {
                time = (float) (!jeiMode ? player.getItemInUseCount()
                                        : (-TickHandler.getNonePausedTicks()) % stack.getUseDuration()) - partialTicks + 1.0F;
            }
            if (time / (float) stack.getUseDuration() < 0.8F) {
                float bobbing = -MathHelper.abs(MathHelper.cos(time / item.getBobbingTickCount() * (float) Math.PI) * 0.1F);

                if (transformType == ItemCameraTransforms.TransformType.GUI)
                    matrixStack.translate(bobbing, bobbing, 0.0F);
                else
                    matrixStack.translate(0.0f, bobbing, 0.0F);
            }

            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.NONE, false, matrixStack, buffer, combinedLight, combinedOverlay, innerModel);

            matrixStack.pop();
        }

        if (firstPerson && player != null) {
            int itemInUseCount = player.getItemInUseCount();
            if (itemInUseCount > 0) {
                int modifier = leftHand ? -1 : 1;
                matrixStack.translate(modifier * .5f, 0, -.25f);
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(modifier * 40));
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(modifier * 10));
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(modifier * 90));
            }
        }

        if (!item.isInteracting(stack))
        {
            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.NONE, false, matrixStack, buffer, combinedLight, combinedOverlay, innerModel);
        }
        else
        {
            final ItemStack target = item.getInteractionTarget(stack);
            itemRenderer.renderItem(target, ItemCameraTransforms.TransformType.NONE, combinedLight, combinedOverlay, matrixStack, buffer);
        }

        matrixStack.pop();
    }
}
