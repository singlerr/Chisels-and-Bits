package mod.chiselsandbits.api.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * All chisels and bits widgets inherit from this class.
 * Most notably provides init support, invoked when the window itself has its init method called.
 */
public class AbstractChiselsAndBitsWidget extends Widget implements IChiselsAndBitsWidget
{
    /**
     * Creates a new widget.
     *
     * @param x The x position.
     * @param y The y position.
     * @param width The width.
     * @param height The height.
     * @param narration The narration text when selected.
     */
    public AbstractChiselsAndBitsWidget(final int x, final int y, final int width, final int height, final ITextComponent narration)
    {
        super(x, y, width, height, narration);
    }

    @NotNull
    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    @NotNull
    public FontRenderer getFont() {
        return Minecraft.getInstance().font;
    }

    @Override
    public void init() {
    }

    @Override
    public void removed() {
    }
}
