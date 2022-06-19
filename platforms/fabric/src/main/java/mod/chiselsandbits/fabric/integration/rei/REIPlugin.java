package mod.chiselsandbits.fabric.integration.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import mod.chiselsandbits.api.item.bit.IBitItem;
import net.minecraft.world.item.ItemStack;

public class REIPlugin implements REIClientPlugin
{
    @Override
    public void registerEntries(final EntryRegistry registry)
    {
        if (!REICompatConfiguration.getInstance().getInjectBits().get()) {
            registry.removeEntryIf(stack -> {
                if (stack.getValue() instanceof ItemStack itemStack) {
                    return itemStack.getItem() instanceof IBitItem;
                }

                return false;
            });
        }
    }
}

