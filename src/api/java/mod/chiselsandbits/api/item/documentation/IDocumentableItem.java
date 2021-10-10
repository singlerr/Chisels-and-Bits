package mod.chiselsandbits.api.item.documentation;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Represents an item that is documented via the Chisels and Bits documentation export system.
 */
public interface IDocumentableItem
{
    /**
     * Gives access to the variants of the item that are exportable.
     * @param item The item instance in question.
     * @return The variants with their names as key.
     */
    default Map<String, ItemStack> getDocumentableInstances(final Item item) {
        return ImmutableMap.of(item.getRegistryName().toString().replace(":", "/"), new ItemStack(item));
    }
}
