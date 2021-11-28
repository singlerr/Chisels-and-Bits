package mod.chiselsandbits.api.client.screen;

import com.google.common.collect.Lists;
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
public class AbstractChiselsAndBitsScreen extends Screen implements IChiselsAndBitsScreen
{
    private boolean isInitialized = false;

    private final List<IChiselsAndBitsWidget> widgets = Lists.newArrayList();
    private final List<Widget> renderables = Lists.newArrayList();

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
    protected <T extends Widget> @NotNull T addRenderableOnly(final T widget)
    {
        final T resultingWidget =  super.addRenderableOnly(widget);
        if (resultingWidget instanceof Widget) {
            this.renderables.add(resultingWidget);
        }

        return resultingWidget;
    }

    @Override
    public <T extends GuiEventListener & NarratableEntry> @NotNull T addWidget(final @NotNull T widget)
    {
        final T resultingWidget = super.addWidget(widget);

        if (isInitialized() && widget instanceof IChiselsAndBitsWidget)
        {
            widgets.add((IChiselsAndBitsWidget) resultingWidget);
            ((IChiselsAndBitsWidget) widget).init();
        }

        if (resultingWidget instanceof Widget) {
            this.renderables.add((Widget) resultingWidget);
        }

        return resultingWidget;
    }

    @Override
    public void removeWidget(final GuiEventListener listener)
    {
        super.removeWidget(listener);
        if (listener instanceof IChiselsAndBitsWidget)
        {
            this.widgets.remove(listener);
            ((IChiselsAndBitsWidget) listener).removed();
        }
        if (listener instanceof Widget) {
            this.renderables.remove(listener);
        }
    }

    @Override
    protected void clearWidgets()
    {
        this.widgets.stream()
          .filter(IChiselsAndBitsWidget.class::isInstance)
          .map(IChiselsAndBitsWidget.class::cast)
          .forEach(IChiselsAndBitsWidget::removed);

        this.widgets.clear();
        this.renderables.clear();
        super.clearWidgets();
    }

    @Override
    public void removed()
    {
        this.widgets.stream()
          .filter(IChiselsAndBitsWidget.class::isInstance)
          .map(IChiselsAndBitsWidget.class::cast)
          .forEach(IChiselsAndBitsWidget::removed);
    }

    @Override
    public boolean isInitialized()
    {
        return isInitialized;
    }

    public List<IChiselsAndBitsWidget> getWidgets()
    {
        return widgets;
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
