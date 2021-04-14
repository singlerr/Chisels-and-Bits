package mod.chiselsandbits.client.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.item.withmode.IRenderableMode;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.network.packets.HeldToolModeChangedPacket;
import mod.chiselsandbits.utils.ItemStackUtils;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("deprecation")
public class RadialToolMenuScreen<M extends IRenderableMode> extends Screen
{
    private static final float DRAWS = 300;

    private static final float INNER = 40, OUTER = 100;
    private static final float SELECT_RADIUS = 10;

    private final IWithModeItem<M> withModeItem;
    private final List<M>          candidates;
    private M selection;

    public static <W extends IRenderableMode> RadialToolMenuScreen<W> create(IWithModeItem<W> modeItem) {
        return new RadialToolMenuScreen<>(modeItem);
    }

    public RadialToolMenuScreen(final IWithModeItem<M> withModeItem) {
        super(TranslationUtils.build("uis.radial-tool-menu"));
        this.withModeItem = withModeItem;
        this.candidates = Lists.newArrayList(withModeItem.getPossibleModes());
        this.selection = withModeItem.getMode(ItemStackUtils.getModeItemStackFromPlayer(Minecraft.getInstance().player));
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTick) {
        // center of screen
        float centerX = Minecraft.getInstance().getMainWindow().getScaledWidth() / 2F;
        float centerY = Minecraft.getInstance().getMainWindow().getScaledHeight() / 2F;

        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        matrix.translate(centerX, centerY, 0);
        RenderSystem.disableTexture();

        // Calculate number of available modes to switch between
        int activeModes = candidates.size();

        // draw base
        RenderSystem.color4f(0.3F, 0.3F, 0.3F, 0.5F);
        drawTorus(matrix, 0, 360);

        RenderSystem.color4f(0.4F, 0.4F, 0.4F, 0.7F);
        int section = candidates.indexOf(selection);
        drawTorus(matrix, -90F + 360F * (-0.5F + section) / activeModes, 360F / activeModes);

        double xDiff = mouseX - centerX;
        double yDiff = mouseY - centerY;
        if (Math.sqrt(xDiff * xDiff + yDiff * yDiff) >= SELECT_RADIUS) {
            // draw mouse selection highlight
            float angle = (float) Math.toDegrees(Math.atan2(yDiff, xDiff));
            RenderSystem.color4f(0.8F, 0.8F, 0.8F, 0.3F);
            drawTorus(matrix, 360F * (-0.5F / activeModes) + angle, 360F / activeModes);

            float selectionAngle = angle + 90F + (360F * (0.5F / activeModes));
            while (selectionAngle < 0) {
                selectionAngle += 360F;
            }
            int selectionDrawnPos = (int) (selectionAngle * (activeModes / 360F));
            selection = candidates.get(selectionDrawnPos);


            // draw hovered selection
            RenderSystem.color4f(0.6F, 0.6F, 0.6F, 0.7F);
            drawTorus(matrix, -90F + 360F * (-0.5F + selectionDrawnPos) / activeModes, 360F / activeModes);
        } else {
            selection = null;
        }

        RenderSystem.color4f(1F, 1F, 1F, 1F);

        // Icons & Labels
        RenderSystem.enableTexture();
        int position = 0;
        for (M instance : candidates) {
            double angle = Math.toRadians(270 + 360 * ((float) position / activeModes));
            float x = (float) Math.cos(angle) * (INNER + OUTER) / 2F;
            float y = (float) Math.sin(angle) * (INNER + OUTER) / 2F;
            // draw icon
            Minecraft.getInstance().textureManager.bindTexture(instance.getIcon());
            blit(matrix, Math.round(x - 12), Math.round(y - 20), 24, 24, 0, 0, 18, 18, 18, 18);
            // draw label
            matrix.push();
            int width = font.getStringPropertyWidth(instance.getShortText());
            matrix.translate(x, y, 0);
            matrix.scale(0.6F, 0.6F, 0.6F);
            font.func_243248_b(matrix, instance.getShortText(), -width / 2F, 8, 0xCCFFFFFF);
            matrix.pop();
            position++;
        }

        RenderSystem.color4f(1F, 1F, 1F, 1F);
        matrix.pop();
    }

    @Override
    public void onClose() {
        updateSelection();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateSelection();
        this.closeScreen();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawTorus(MatrixStack matrix, float startAngle, float sizeAngle) {
        BufferBuilder vertexBuffer = Tessellator.getInstance().getBuffer();
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        vertexBuffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        float draws = DRAWS * (sizeAngle / 360F);
        for (int i = 0; i <= draws; i++) {
            float angle = (float) Math.toRadians(startAngle + (i / DRAWS) * 360);
            vertexBuffer.pos(matrix4f, (float) (OUTER * Math.cos(angle)), (float) (OUTER * Math.sin(angle)), 0).endVertex();
            vertexBuffer.pos(matrix4f, (float) (INNER * Math.cos(angle)), (float) (INNER * Math.sin(angle)), 0).endVertex();
        }
        vertexBuffer.finishDrawing();
        WorldVertexBufferUploader.draw(vertexBuffer);
    }

    public void updateSelection() {
        if (selection != null) {
            withModeItem.setMode(
              ItemStackUtils.getModeItemStackFromPlayer(Minecraft.getInstance().player),
              selection
            );

            ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new HeldToolModeChangedPacket(candidates.indexOf(selection)));
        }
    }

    public void onMoveSelectionToTheRight()
    {
        if (selection != null) {
            int workingIndex = candidates.indexOf(selection);
            workingIndex ++;
            if (workingIndex >= candidates.size()) {
                workingIndex = 0;
            }

            selection = candidates.get(workingIndex);
        }
    }

    public void onMoveSelectionToTheLeft()
    {
        if (selection != null) {
            int workingIndex = candidates.indexOf(selection);
            workingIndex --;
            if (workingIndex < 0) {
                workingIndex = candidates.size() - 1;
            }

            selection = candidates.get(workingIndex);
        }
    }
}
