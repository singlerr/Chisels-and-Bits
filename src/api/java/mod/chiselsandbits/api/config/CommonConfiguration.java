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
        createCategory(builder, "common.help");

        enableHelp = defineBoolean(builder, "common.help.enabled", true);

        finishCategory(builder);

        createCategory(builder, "common.performance");

        collisionBoxCacheSize = defineLong(builder, "common.performance.collisions.cache.size", 10000L);

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
