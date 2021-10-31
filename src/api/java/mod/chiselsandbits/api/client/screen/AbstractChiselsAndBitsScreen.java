package mod.chiselsandbits.api.client.screen;

import mod.chiselsandbits.api.client.screen.widget.IChiselsAndBitsWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

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
    protected AbstractChiselsAndBitsScreen(final ITextComponent narrationMessage)
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
    public <T extends Widget> @NotNull T addButton(final @NotNull T button)
    {
        return super.addButton(button);
    }

    @Override
    public <T extends IGuiEventListener> @NotNull T addWidget(final @NotNull T widget)
    {
        final T resultingWidget = super.addWidget(widget);
        if (isInitialized() && widget instanceof IChiselsAndBitsWidget)
            ((IChiselsAndBitsWidget) widget).init();

        return resultingWidget;
    }

    public List<Widget> getButtons() {
        return buttons;
    }

    public List<IGuiEventListener> getWidgets() {
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

}
