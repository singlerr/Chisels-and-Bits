package mod.chiselsandbits.registrars;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class ModItemGroups
{
    public static final  CreativeModeTab CHISELS_AND_BITS = new CreativeModeTab(CreativeModeTab.TABS.length, Constants.MOD_ID)
    {
        @Override
        public @NotNull ItemStack makeIcon()
        {
            return new ItemStack(ModItems.ITEM_CHISEL_STONE.get());
        }
    };
    private static final Logger          LOGGER           = LogManager.getLogger();

    private ModItemGroups()
    {
        throw new IllegalStateException("Tried to initialize: ModItemGroups but this is a Utility class.");
    }

    public static void onModConstruction()
    {
        LOGGER.info("Loaded item group configuration.");
    }

}
