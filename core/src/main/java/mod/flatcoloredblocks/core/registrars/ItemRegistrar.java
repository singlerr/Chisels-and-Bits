package com.communi.suggestu.formula.core.registrars;

import com.communi.suggestu.formula.core.util.Constants;
import com.communi.suggestu.scena.core.registries.deferred.IRegistrar;
import com.communi.suggestu.scena.core.registries.deferred.IRegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemRegistrar
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemRegistrar.class);

    private ItemRegistrar()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ItemRegistrar. This is a utility class");
    }

    private final static IRegistrar<Item> ITEM_REGISTRAR               = IRegistrar.create(Registry.ITEM_REGISTRY, Constants.MOD_ID);

    private final static IRegistryObject<Item> TEST_ITEM = ITEM_REGISTRAR.register("test_item", () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS).stacksTo(1)));

    public static void onModConstruction() {
        LOGGER.info("Registering items");
    }
}
