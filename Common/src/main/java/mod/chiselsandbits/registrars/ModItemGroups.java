package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ModItemGroups
{

    private ModItemGroups()
    {
        throw new IllegalStateException("Tried to initialize: ModItemGroups but this is a Utility class.");
    }

    public static final CreativeModeTab CHISELS_AND_BITS = new CreativeModeTab(CreativeModeTab.TABS.length, Constants.MOD_ID) {
        @Override
        public @NotNull ItemStack makeIcon()
        {
            return new ItemStack(ModItems.ITEM_CHISEL_STONE.get());
        }
    };
}
