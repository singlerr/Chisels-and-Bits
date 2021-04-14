package mod.chiselsandbits.api.item.withmode;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface IRenderableMode
{

    ResourceLocation getIcon();

    ITextComponent getShortText();
}
