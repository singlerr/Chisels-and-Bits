package mod.chiselsandbits.api.config;

import mod.chiselsandbits.api.util.LocalStrings;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfiguration extends AbstractConfiguration
{

    public ForgeConfigSpec.BooleanValue enableHelp;
    public ForgeConfigSpec.LongValue collisionBoxCacheSize;

    public CommonConfiguration(ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "help");

        enableHelp = defineBoolean(builder, "enabled-in-tooltips", true);

        swapToCategory(builder, "performance.caches.sizes");

        collisionBoxCacheSize = defineLong(builder, "collision-boxes", 10000L);

        finishCategory(builder);
    }


    public void helpText(
      final LocalStrings string,
      final List<Component> tooltip,
      final Object... variables )
    {
        if ( enableHelp.get() )
        {
            tooltip.add(string.getText(variables));
        }
    }
}
