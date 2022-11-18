package mod.chiselsandbits.api.client.screen.widget;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;

public interface IChiselsAndBitsWidget extends Widget, GuiEventListener
{
    /**
     * Invoked by the screen, when said screen is initialized.
     */
    void init();

    /**
     * Invoked by the screen, when it is removed from the display.
     */
    void removed();
}
