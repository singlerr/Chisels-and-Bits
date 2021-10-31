package mod.chiselsandbits.api.client.screen.widget;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;

public interface IChiselsAndBitsWidget extends IRenderable, IGuiEventListener
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
