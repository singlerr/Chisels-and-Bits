package mod.chiselsandbits.api.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.client.screen.widget.IChiselsAndBitsWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom screens which inherit from this class implement custom logic related to chisels and bits widgets and buttons.
 */
public class AbstractChiselsAndBitsScreen extends Screen
{
    private boolean isInitialized = false;

    /**
     * Creates a new screen, playing the narration message when opened.
     * @param narrationMessage The narration message for the screen.
     */
    protected AbstractChiselsAndBitsScreen(final Component narrationMessage)
    {
        super(narrationMessage);
    }

    @Override
    protected void init()
    {
        super.init();
        this.isInitialized = true;

        this.children().stream().filter(IChiselsAndBitsWidget.class::isInstance)
          .map(IChiselsAndBitsWidget.class::cast)
          .forEach(IChiselsAndBitsWidget::init);
    }

    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> @NotNull T addRenderableWidget(final @NotNull T button)
    {
        return super.addRenderableWidget(button);
    }

    @Override
    public <T extends GuiEventListener & NarratableEntry> @NotNull T addWidget(final @NotNull T widget)
    {
        final T resultingWidget = super.addWidget(widget);
        if (isInitialized() && widget instanceof IChiselsAndBitsWidget)
            ((IChiselsAndBitsWidget) widget).init();

        return resultingWidget;
    }

    public List<Widget> getButtons() {
        return renderables;
    }

    public List<GuiEventListener> getWidgets() {
        return children;
    }

    @Override
    public void removed()
    {
        getWidgets().stream()
          .filter(IChiselsAndBitsWidget.class::isInstance)
          .map(IChiselsAndBitsWidget.class::cast)
          .forEach(IChiselsAndBitsWidget::removed);
    }

    public boolean isInitialized()
    {
        return isInitialized;
    }

    @Override
    public void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTickTime)
    {
        final  List<Widget> renderTargets = new ArrayList<>(this.renderables);
        for(Widget widget : renderTargets) {
            widget.render(poseStack, mouseX, mouseY, partialTickTime);
        }
    }
}
