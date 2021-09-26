package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModItemGroups
{

    private ModItemGroups()
    {
        throw new IllegalStateException("Tried to initialize: ModItemGroups but this is a Utility class.");
    }

    public static final CreativeModeTab CHISELS_AND_BITS = new CreativeModeTab(Constants.MOD_ID) {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(ModItems.ITEM_CHISEL_STONE.get());
        }
    };
}
