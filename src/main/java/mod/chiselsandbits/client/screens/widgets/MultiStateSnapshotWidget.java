package mod.chiselsandbits.client.screens.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.ColorUtils;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.multistate.snapshot.EmptySnapshot;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class MultiStateSnapshotWidget extends Widget
{

    private static double GUISCALE;
    private ItemStack snapshotBlockStack = ItemStack.EMPTY;

    private Vector3d facingVector = Vector3d.ZERO;
    private float scaleFactor = 1f;

    public MultiStateSnapshotWidget(final int x, final int y, final int width, final int height, final ITextComponent title)
    {
        super(x, y, width, height, title);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(final @NotNull MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        //super.renderButton(matrixStack, mouseX, mouseY, partialTicks);

        fill(matrixStack, this.x, this.y, this.x + this.width, this.y + this.height, ColorUtils.pack(139));
        fill(matrixStack, this.x, this.y, this.x + this.width - 1, this.y + this.height - 1, ColorUtils.pack(55));
        fill(matrixStack, this.x + 1, this.y + 1, this.x + this.width, this.y + this.height, ColorUtils.pack(ColorUtils.FULL_CHANNEL));
        fill(matrixStack, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, ColorUtils.pack(ColorUtils.EMPTY_CHANNEL));

        MainWindow mw = Minecraft.getInstance().getWindow();
        double sf = mw.getGuiScale();
        GL11.glScissor((int)((this.x) * mw.getGuiScale()), (int)(mw.getGuiScaledHeight() * sf - height * sf - (y) * sf), (int)(width * sf), (int)(height * sf));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        if (!snapshotBlockStack.isEmpty()) {
            RenderSystem.pushMatrix();
            renderRotateableItemAndEffectIntoGui();
            RenderSystem.popMatrix();
        }

        scissorEnd();
    }

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    public void renderRotateableItemAndEffectIntoGui() {
        final int x = this.x + this.width / 2 - 8;
        final int y = this.y + this.height / 2 - 8;
        final IBakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(
          snapshotBlockStack,
          Minecraft.getInstance().level,
          Minecraft.getInstance().player
        );

        RenderSystem.pushMatrix();
        Minecraft.getInstance().getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);
        Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.translatef((float)x, (float)y, 150);
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.rotatef(
          (float) (this.facingVector.x() * VectorUtils.DEG_TO_RAD_FACTOR),
          1,0,0
        );
        RenderSystem.rotatef(
          (float) (this.facingVector.y() * VectorUtils.DEG_TO_RAD_FACTOR),
          0, 1,0
        );
        RenderSystem.rotatef(
          (float) (this.facingVector.z() * VectorUtils.DEG_TO_RAD_FACTOR),
          0,0,1
        );
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);
        RenderSystem.scalef(scaleFactor, scaleFactor, scaleFactor);
        MatrixStack matrixstack = new MatrixStack();
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !bakedmodel.usesBlockLight();
        if (flag) {
            RenderHelper.setupForFlatItems();
        }

        Minecraft.getInstance().getItemRenderer().render(snapshotBlockStack, ItemCameraTransforms.TransformType.GUI, false, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
        irendertypebuffer$impl.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            RenderHelper.setupFor3DItems();
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
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
        this.facingVector = this.facingVector.add(-dragY * 100, dragX * 100, 0);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double delta)
    {
        this.scaleFactor += delta * 0.25;
        return true;
    }

    private static void enableScissor(final int x, final int y, final int w, final int h)
    {
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, w, h);
    }

    protected void scissorEnd() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
