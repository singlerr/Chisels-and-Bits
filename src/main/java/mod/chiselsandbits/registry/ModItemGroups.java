package mod.chiselsandbits.registry;

import mod.chiselsandbits.client.ModItemGroup;
import net.minecraft.item.ItemGroup;

public final class ModItemGroups
{

    private ModItemGroups()
    {
        throw new IllegalStateException("Tried to initialize: ModItemGroups but this is a Utility class.");
    }

    public static final ItemGroup CHISELS_AND_BITS = new ModItemGroup();
}
