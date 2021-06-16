package mod.chiselsandbits.client.screens;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.item.withmode.IRenderableMode;
import mod.chiselsandbits.api.item.withmode.IToolMode;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.network.packets.HeldToolModeChangedPacket;
import mod.chiselsandbits.utils.ItemStackUtils;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class RadialToolModeSelectionScreen<M extends IToolMode<?>> extends Screen
{
    private static final float DRAWS = 720;

    private static final float MAIN_TORUS_INNER = 40, MAIN_TORUS_OUTER = 80;
    private static final float OUTER_TORUS_INNER = 85, OUTER_TORUS_OUTER = 120;
    private static final float MAIN_SELECT_RADIUS  = 10;
    private static final float OUTER_SELECT_RADIUS = 83;

    private static final int DEFAULT_ICON_SIZE        = 16;
    private static final int DEFAULT_ICON_TEXT_SPACER = 6;

    private final IWithModeItem<M> toolModeItem;
    private final ItemStack        sourceStack;

    private final LinkedHashSet<IRenderableMode>        mainToolModes    = Sets.newLinkedHashSet();
    private final LinkedList<IRenderableMode>           mainToolModeList = Lists.newLinkedList();
    private final LinkedListMultimap<IToolModeGroup, M> outerToolModes   = LinkedListMultimap.create();

    private final List<M>         candidates = Lists.newLinkedList();
    private       IRenderableMode lastFrameMainSelectedToolMode;
    private       IRenderableMode mainSelectedToolMode;
    private       M               selectedOuterToolMode;

    private RadialToolModeSelectionScreen(final IWithModeItem<M> toolModeItem, final ItemStack sourceStack, final ITextComponent itemName)
    {
        super(TranslationUtils.build("guis.titles.tool-mode", itemName));

        this.toolModeItem = toolModeItem;
        this.sourceStack = sourceStack;

        this.candidates.addAll(this.toolModeItem.getPossibleModes());
        this.candidates.forEach(candidate -> {
            if (candidate.getGroup().isPresent())
            {
                outerToolModes.put(candidate.getGroup().get(), candidate);
                mainToolModes.add(candidate.getGroup().get());
            }
            else
            {
                mainToolModes.add(candidate);
            }
        });

        final M currentMode = toolModeItem.getMode(this.sourceStack);
        if (currentMode.getGroup().isPresent())
        {
            this.mainSelectedToolMode = currentMode.getGroup().get();
            this.selectedOuterToolMode = currentMode;
        }
        else
        {
            this.mainSelectedToolMode = currentMode;
            this.selectedOuterToolMode = null;
        }
        this.lastFrameMainSelectedToolMode = this.mainSelectedToolMode;

        this.mainToolModeList.addAll(this.mainToolModes);
    }

    public static <W extends IToolMode<?>> RadialToolModeSelectionScreen<W> create(IWithModeItem<W> modeItem, final ItemStack stack)
    {
        return new RadialToolModeSelectionScreen<>(modeItem, stack, stack.getDisplayName());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        // center of screen
        float centerX = Minecraft.getInstance().getMainWindow().getScaledWidth() / 2F;
        float centerY = Minecraft.getInstance().getMainWindow().getScaledHeight() / 2F;

        matrixStack.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        matrixStack.translate(centerX, centerY, 0);
        RenderSystem.disableTexture();

        renderTorus(matrixStack, mouseX, mouseY, centerX, centerY);

        RenderSystem.color4f(1F, 1F, 1F, 1F);
        matrixStack.pop();
    }

    @SuppressWarnings("unchecked")
    private void renderTorus(final @NotNull MatrixStack matrix, final int mouseX, final int mouseY, final float centerX, final float centerY)
    {
        handleSelectableTorusRendering(
          matrix,
          mouseX,
          mouseY,
          centerX,
          centerY,
          MAIN_TORUS_INNER,
          MAIN_TORUS_OUTER,
          MAIN_SELECT_RADIUS,
          OUTER_TORUS_OUTER,
          0f,
          360f,
          1f,
          this.mainToolModeList,
          () -> this.mainSelectedToolMode,
          this::onMainToolModeHover,
          font
        );

        if (this.selectedOuterToolMode != null && this.mainSelectedToolMode instanceof IToolModeGroup)
        {
            handleSelectableTorusRendering(
              matrix,
              mouseX,
              mouseY,
              centerX,
              centerY,
              OUTER_TORUS_INNER,
              OUTER_TORUS_OUTER,
              OUTER_SELECT_RADIUS,
              OUTER_TORUS_OUTER,
              (this.mainToolModeList.indexOf(this.mainSelectedToolMode) * (360f / this.mainToolModeList.size())),
              (360f / this.mainToolModeList.size()),
              0.6f,
              this.outerToolModes.get((IToolModeGroup) this.mainSelectedToolMode),
              () -> this.selectedOuterToolMode,
              (mode) -> {
                  if (mode != null)
                  {
                      this.selectedOuterToolMode = (M) mode;
                  }
              },
              font
            );
        }
    }

    private static void drawTorus(MatrixStack matrix, float startAngle, float sizeAngle, float inner, float outer)
    {
        BufferBuilder vertexBuffer = Tessellator.getInstance().getBuffer();
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        vertexBuffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        float draws = DRAWS * (sizeAngle / 360F);
        for (int i = 0; i <= draws; i++)
        {
            float angle = (float) Math.toRadians(startAngle + (i / DRAWS) * 360);
            vertexBuffer.pos(matrix4f, (float) (outer * Math.cos(angle)), (float) (outer * Math.sin(angle)), 0).endVertex();
            vertexBuffer.pos(matrix4f, (float) (inner * Math.cos(angle)), (float) (inner * Math.sin(angle)), 0).endVertex();
        }
        vertexBuffer.finishDrawing();
        WorldVertexBufferUploader.draw(vertexBuffer);
    }

    private static void handleSelectableTorusRendering(
      @NotNull final MatrixStack matrixStack,
      final int mouseX,
      final int mouseY,
      final float centerX,
      final float centerY,
      final float innerRadius,
      final float outerRadius,
      final float innerSelectionRadius,
      final float outerSelectionRadius,
      final float startAngle,
      final float sectionArcAngle,
      final float iconScaleFactor,
      final List<? extends IRenderableMode> candidates,
      final Supplier<IRenderableMode> currentGetter,
      final Consumer<IRenderableMode> currentSetter,
      final FontRenderer font
    )
    {
        drawSelectableOptions(
          matrixStack,
          startAngle,
          sectionArcAngle,
          innerRadius,
          outerRadius,
          innerSelectionRadius,
          outerSelectionRadius,
          mouseX,
          mouseY,
          centerX,
          centerY,
          font,
          iconScaleFactor,
          candidates,
          currentGetter,
          currentSetter
        );
    }

    private void onMainToolModeHover(final IRenderableMode mode)
    {
        this.mainSelectedToolMode = mode;

        if (this.lastFrameMainSelectedToolMode == null && this.mainSelectedToolMode == null)
        {
            return;
        }

        if (this.lastFrameMainSelectedToolMode != null && this.mainSelectedToolMode == null)
        {
            this.lastFrameMainSelectedToolMode = null;
            this.selectedOuterToolMode = null;
            return;
        }

        if ((this.lastFrameMainSelectedToolMode == null || this.lastFrameMainSelectedToolMode != this.mainSelectedToolMode) && this.mainSelectedToolMode instanceof IToolModeGroup)
        {
            this.lastFrameMainSelectedToolMode = this.mainSelectedToolMode;
            this.selectedOuterToolMode = this.outerToolModes.get((IToolModeGroup) this.mainSelectedToolMode).get(0);
            return;
        }

        if (this.mainSelectedToolMode instanceof IToolMode)
        {
            this.lastFrameMainSelectedToolMode = this.mainSelectedToolMode;
            this.selectedOuterToolMode = null;
        }
    }

    private static void drawSelectableOptions(
      @NotNull final MatrixStack stack,
      final float sectionStartAngle,
      final float sectionArcAngle,
      final float innerRadius,
      final float outerRadius,
      final float innerSelectionRadius,
      final float outerSelectionRadius,
      final float mouseX,
      final float mouseY,
      final float centerX,
      final float centerY,
      @NotNull final FontRenderer fontRenderer,
      final float iconScaleFactor,
      @NotNull final List<? extends IRenderableMode> modes,
      @NotNull final Supplier<IRenderableMode> currentlySelectedModeSupplier,
      @NotNull final Consumer<IRenderableMode> currentlyHoveredModeCallback
    )
    {
        final IRenderableMode current = currentlySelectedModeSupplier.get();

        final int selectableItemCount = modes.size();
        if (selectableItemCount == 0)
        {
            return;
        }

        final float itemArcAngle = sectionArcAngle / selectableItemCount;

        final float mouseAngle = calculateMouseAngle(mouseX, mouseY, centerX, centerY);
        final float mouseRadius = calculateMouseRadius(mouseX, mouseY, centerX, centerY);

        final float inSectionMouseAngle = mouseAngle - sectionStartAngle;
        final boolean mouseIsInSectionArc = inSectionMouseAngle >= 0 && inSectionMouseAngle <= sectionArcAngle;
        final boolean isMouseInSection = mouseIsInSectionArc && mouseRadius >= innerSelectionRadius && mouseRadius <= outerSelectionRadius;

        final int hoveredItemIndex = !isMouseInSection ? -1 : (int) (inSectionMouseAngle / itemArcAngle);

        RenderSystem.enableBlend();
        modes.forEach(mode -> {
            final int modeIndex = modes.indexOf(mode);
            final boolean isSelected = current != null && mode == current;
            final boolean isHovered = hoveredItemIndex == modeIndex;

            final float itemTargetAngle = ((modeIndex + 0.5f) * itemArcAngle) + sectionStartAngle;

            drawSelectableSection(
              stack,
              sectionArcAngle,
              innerRadius,
              outerRadius,
              selectableItemCount,
              itemTargetAngle,
              isSelected,
              isHovered
            );
        });
        RenderSystem.disableBlend();

        if (isMouseInSection)
        {

            if (Configuration.getInstance().getClient().enableMouseIndicatorInRadialMenu.get()) {
                float startOfMouseArcAngle = mouseAngle - (itemArcAngle / 2);
                float mouseArcAngle = itemArcAngle;

                if (sectionArcAngle != 360) {
                    if (startOfMouseArcAngle < sectionStartAngle) {
                        mouseArcAngle -= (sectionStartAngle - startOfMouseArcAngle);
                        startOfMouseArcAngle = sectionStartAngle;
                    }

                    if ((startOfMouseArcAngle + mouseArcAngle) > (sectionStartAngle + sectionArcAngle)) {
                        mouseArcAngle = (sectionStartAngle + sectionArcAngle) - startOfMouseArcAngle;
                    }
                }

                RenderSystem.color4f(0.8F, 0.8F, 0.8F, 0.3F);
                drawTorus(stack, startOfMouseArcAngle - 90, mouseArcAngle, innerRadius, outerRadius);
            }

            if (hoveredItemIndex >= 0)
            {
                currentlyHoveredModeCallback.accept(modes.get(hoveredItemIndex));
            }
        }
        else
        {
            currentlyHoveredModeCallback.accept(null);
        }

        RenderSystem.enableTexture();
        modes.forEach(mode -> {
            final int modeIndex = modes.indexOf(mode);
            final float itemTargetAngle = ((modeIndex + 0.5f) * itemArcAngle) + sectionStartAngle;
            renderModeIcon(stack, innerRadius, outerRadius, itemTargetAngle, iconScaleFactor, mode, fontRenderer);
        });
        RenderSystem.disableTexture();
    }

    private static float calculateMouseAngle(final float mouseX, final float mouseY, final float centerX, final float centerY)
    {
        final float xDiff = mouseX - centerX;
        final float yDiff = mouseY - centerY;

        float mouseAngle = (float) Math.toDegrees(Math.atan2(yDiff, xDiff)) - 270f;
        while (mouseAngle < 0)
        {
            mouseAngle += 360;
        }
        return mouseAngle;
    }

    private static float calculateMouseRadius(final float mouseX, final float mouseY, final float centerX, final float centerY)
    {
        final float xDiff = mouseX - centerX;
        final float yDiff = mouseY - centerY;

        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    @SuppressWarnings("deprecation")
    private static void drawSelectableSection(
      @NotNull final MatrixStack stack,
      final float sectionArcAngle,
      final float innerRadius,
      final float outerRadius,
      final int itemCountInSection,
      final float itemTargetAngle,
      final boolean isSelected,
      final boolean isHovered
    )
    {
        final float itemArcAngle = sectionArcAngle / itemCountInSection;
        final float itemRenderAngle = itemTargetAngle - 90F;

        final float sectionStartAngle = itemRenderAngle - (itemArcAngle / 2);

        RenderSystem.color4f(0.3f, 0.3f, 0.3f, 0.3f);
        drawTorus(
          stack,
          sectionStartAngle,
          itemArcAngle,
          innerRadius,
          outerRadius
        );

        if (isSelected)
        {
            RenderSystem.color4f(0.4F, 0.4F, 0.4F, 0.7F);
            drawTorus(
              stack,
              sectionStartAngle,
              itemArcAngle,
              innerRadius,
              outerRadius
            );
        }

        if (isHovered)
        {
            RenderSystem.color4f(0.7F, 0.7F, 0.7F, 0.7F);
            drawTorus(
              stack,
              sectionStartAngle,
              itemArcAngle,
              innerRadius,
              outerRadius
            );
        }
    }

    private static void renderModeIcon(
      final @NotNull MatrixStack stack,
      final float innerRadius,
      final float outerRadius,
      final float itemTargetAngle,
      final float iconScaleFactor,
      final @NotNull IRenderableMode mode,
      final FontRenderer fontRenderer)
    {
        float workingAngle = itemTargetAngle - 90;
        while (workingAngle < 0)
        {
            workingAngle += 360;
        }

        final float itemCenterX = (float) Math.cos(Math.toRadians(workingAngle)) * (innerRadius + outerRadius) / 2F;
        final float itemCenterY = (float) Math.sin(Math.toRadians(workingAngle)) * (innerRadius + outerRadius) / 2F;

        final ITextComponent name = mode.getDisplayName();
        final int fontWidth = fontRenderer.getStringPropertyWidth(name);

        final int itemHeight = mode.shouldRenderDisplayNameInMenu() ?
                                 (int) ((DEFAULT_ICON_SIZE * iconScaleFactor) + DEFAULT_ICON_TEXT_SPACER + fontRenderer.FONT_HEIGHT)
                                 : (int) (DEFAULT_ICON_SIZE * iconScaleFactor);

        final float iconStartX = itemCenterX - ((DEFAULT_ICON_SIZE * iconScaleFactor) / 2f);
        final float iconStartY = itemCenterY - (itemHeight / 2f);

        stack.push();
        RenderSystem.color4f(
          (float) mode.getColorVector().getX(),
          (float) mode.getColorVector().getY(),
          (float) mode.getColorVector().getZ(),
          1
        );
        Minecraft.getInstance().getTextureManager().bindTexture(mode.getIcon());
        blit(stack, (int) iconStartX, (int) iconStartY, (int) (DEFAULT_ICON_SIZE * iconScaleFactor), (int) (DEFAULT_ICON_SIZE * iconScaleFactor), 0, 0, 18, 18, 18, 18);
        stack.push();

        if (mode.shouldRenderDisplayNameInMenu()) {
            stack.translate(itemCenterX, itemCenterY, 0);
            stack.scale(0.6F * iconScaleFactor, 0.6F * iconScaleFactor, 0.6F * iconScaleFactor);
            fontRenderer.func_243248_b(stack, mode.getDisplayName(), fontWidth / -2f, DEFAULT_ICON_TEXT_SPACER, 0xCCFFFFFF);
        }

        stack.pop();
        stack.pop();
    }

    @Override
    public void onClose()
    {
        updateSelection();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @SuppressWarnings("unchecked")
    public void updateSelection()
    {
        if (this.mainSelectedToolMode != null)
        {
            final M mainToolModeSelection =
              this.mainSelectedToolMode instanceof IToolModeGroup ? this.selectedOuterToolMode : (this.mainSelectedToolMode instanceof IToolMode) ? (M) this.mainSelectedToolMode
                                                                                                   : toolModeItem.getMode(this.sourceStack);

            toolModeItem.setMode(
              ItemStackUtils.getModeItemStackFromPlayer(Minecraft.getInstance().player),
              mainToolModeSelection
            );

            ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new HeldToolModeChangedPacket(candidates.indexOf(mainToolModeSelection)));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        updateSelection();
        this.closeScreen();
        return true;
    }

    @SuppressWarnings("unchecked")
    public void onMoveSelectionToTheRight()
    {
        if (mainSelectedToolMode != null)
        {
            final M mainToolModeSelection =
              this.mainSelectedToolMode instanceof IToolModeGroup ? this.selectedOuterToolMode : (this.mainSelectedToolMode instanceof IToolMode) ? (M) this.mainSelectedToolMode
                                                                                                   : toolModeItem.getMode(this.sourceStack);
            int workingIndex = candidates.indexOf(mainToolModeSelection);
            workingIndex++;
            if (workingIndex >= candidates.size())
            {
                workingIndex = 0;
            }

            final M newMode = candidates.get(workingIndex);
            if (newMode.getGroup().isPresent())
            {
                this.mainSelectedToolMode = newMode.getGroup().get();
                this.selectedOuterToolMode = newMode;
            }
            else
            {
                this.mainSelectedToolMode = newMode;
                this.selectedOuterToolMode = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void onMoveSelectionToTheLeft()
    {
        if (mainSelectedToolMode != null)
        {
            final M mainToolModeSelection =
              this.mainSelectedToolMode instanceof IToolModeGroup ? this.selectedOuterToolMode : (this.mainSelectedToolMode instanceof IToolMode) ? (M) this.mainSelectedToolMode
                                                                                                   : toolModeItem.getMode(this.sourceStack);
            int workingIndex = candidates.indexOf(mainToolModeSelection);
            workingIndex--;
            if (workingIndex < 0)
            {
                workingIndex = candidates.size() - 1;
            }

            final M newMode = candidates.get(workingIndex);
            if (newMode.getGroup().isPresent())
            {
                this.mainSelectedToolMode = newMode.getGroup().get();
                this.selectedOuterToolMode = newMode;
            }
            else
            {
                this.mainSelectedToolMode = newMode;
                this.selectedOuterToolMode = null;
            }
        }
    }
}
