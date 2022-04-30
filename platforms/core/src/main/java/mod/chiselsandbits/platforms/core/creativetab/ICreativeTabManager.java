package mod.chiselsandbits.platforms.core.creativetab;

import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Manager which is used to handle the creation of new creative tabs.
 */
public interface ICreativeTabManager
{

    /**
     * The current instance of on the current platform.
     *
     * @return The current manager.
     */
    static ICreativeTabManager getInstance() {
        return IChiselsAndBitsPlatformCore.getInstance().getCreativeTabManager();
    }

    /**
     * Registers a new creative tab made by the builder.
     *
     * @param builder The builder for the tab.
     * @return The newly registered tab.
     */
    CreativeModeTab register(final IntFunction<CreativeModeTab> builder);
}
