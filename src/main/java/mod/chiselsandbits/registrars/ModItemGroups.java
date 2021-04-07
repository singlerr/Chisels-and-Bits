package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class ModItemGroups
{

    private ModItemGroups()
    {
        throw new IllegalStateException("Tried to initialize: ModItemGroups but this is a Utility class.");
    }

    public static final ItemGroup CHISELS_AND_BITS = new ItemGroup(Constants.MOD_ID) {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(ModItems.ITEM_CHISEL_STONE.get());
        }
    };
}
