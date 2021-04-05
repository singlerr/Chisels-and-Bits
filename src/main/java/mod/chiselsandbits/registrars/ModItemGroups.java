package mod.chiselsandbits.registrars;

import mod.chiselsandbits.client.ModItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class ModItemGroups
{

    private ModItemGroups()
    {
        throw new IllegalStateException("Tried to initialize: ModItemGroups but this is a Utility class.");
    }

    public static final ItemGroup CHISELS_AND_BITS = new ItemGroup() {
        @Override
        public ItemStack createIcon()
        {
            return ModItems.ITEM_CHISEL_GOLD
        }
    };
}
