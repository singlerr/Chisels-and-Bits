package mod.chiselsandbits.client.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.client.screen.widget.AbstractChiselsAndBitsWidget;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.ColorUtils;
import mod.chiselsandbits.multistate.snapshot.EmptySnapshot;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Lighting;
public class MultiStateSnapshotWidget extends AbstractChiselsAndBitsWidget
{

    private static double GUISCALE;
    private ItemStack snapshotBlockStack = ItemStack.EMPTY;

    private Vec3 facingVector = Vec3.ZERO;
    private float scaleFactor = 1f;

    public MultiStateSnapshotWidget(final int x, final int y, final int width, final int height, final Component title)
    {
        super(x, y, width, height, title);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(final @NotNull PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        fill(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, ColorUtils.pack(139));
        fill(poseStack, this.x, this.y, this.x + this.width - 1, this.y + this.height - 1, ColorUtils.pack(55));
        fill(poseStack, this.x + 1, this.y + 1, this.x + this.width, this.y + this.height, ColorUtils.pack(ColorUtils.FULL_CHANNEL));
        fill(poseStack, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, ColorUtils.pack(ColorUtils.EMPTY_CHANNEL));

        scissorStart();

        if (!snapshotBlockStack.isEmpty()) {
            poseStack.pushPose();
            renderRotateableItemAndEffectIntoGui();
            poseStack.popPose();
        }

        scissorEnd();
    }

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    public void renderRotateableItemAndEffectIntoGui(
    ) {
        final int x = this.x + this.width / 2 - 8;
        final int y = this.y + this.height / 2 - 8;
        final BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(
          snapshotBlockStack,
          Minecraft.getInstance().level,
          Minecraft.getInstance().player,
          0
        );

        final PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        final float blitOffset = Minecraft.getInstance().getItemRenderer().blitOffset;
        poseStack.translate((float)x, (float)y, 150);
        poseStack.translate(8.0F, 8.0F, 0.0F);
        poseStack.mulPose(Quaternion.fromXYZDegrees(
          new Vector3f(this.facingVector)
        ));
        poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
        poseStack.translate(-8.0F, -8.0F, 0.0F);

        poseStack.translate(0,0, -100-blitOffset);
        RenderSystem.applyModelViewMatrix();

        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !bakedmodel.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        Minecraft.getInstance().getItemRenderer().renderGuiItem(snapshotBlockStack, 0, 0, bakedmodel);

        irendertypebuffer$impl.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public IMultiStateSnapshot getSnapshot()
    {
        if (!(snapshotBlockStack.getItem() instanceof IMultiStateItem))
            return EmptySnapshot.INSTANCE;

        return ((IMultiStateItem) snapshotBlockStack.getItem()).createItemStack(snapshotBlockStack).createSnapshot();
    }

    public void setSnapshot(final IMultiStateSnapshot snapshot)
    {
        this.snapshotBlockStack = snapshot.toItemStack().toBlockStack();
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onDrag(final double mouseX, final double mouseY, final double dragX, final double dragY)
    {
        this.facingVector = this.facingVector.add(-dragY * 10, dragX * 10, 0);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double delta)
    {
        this.scaleFactor += delta * 0.25;
        return true;
    }

    private void scissorStart()
    {
        Window mw = Minecraft.getInstance().getWindow();
        double sf = mw.getGuiScale();
        GL11.glScissor((int)((this.x + 1) * mw.getGuiScale()), (int)(mw.getGuiScaledHeight() * sf - height * sf - (y - 1) * sf), (int)((width - 2) * sf), (int)((height - 1) * sf));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    protected void scissorEnd() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
