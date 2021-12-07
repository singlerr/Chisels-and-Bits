package mod.chiselsandbits.fabric.platform.registry.registar;

import mod.chiselsandbits.fabric.platform.registry.registar.delegates.FabricVanillaRegistryRegistrarDelegate;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistry;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistryEntry;
import mod.chiselsandbits.platforms.core.registries.deferred.ICustomRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrarManager;
import mod.chiselsandbits.platforms.core.registries.deferred.impl.custom.CustomRegistryManager;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

public final class FabricRegistrarManager implements IRegistrarManager
{
    private static final FabricRegistrarManager INSTANCE = new FabricRegistrarManager();

    public static FabricRegistrarManager getInstance()
    {
        return INSTANCE;
    }

    private FabricRegistrarManager()
    {
    }

    @Override
    public <T extends IChiselsAndBitsRegistryEntry, R extends T> ICustomRegistrar<R> createCustomRegistrar(
      final Class<T> typeClass, final String modId)
    {
        return CustomRegistryManager.getInstance().createNewRegistrar(typeClass, modId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, R extends T> IRegistrar<R> createRegistrar(final Class<T> typeClass, final String modId)
    {
        if (typeClass == Item.class) {
            return new FabricVanillaRegistryRegistrarDelegate<>(modId, (Registry<T>) Registry.ITEM);
        }

        if (typeClass == Block.class) {
            return new FabricVanillaRegistryRegistrarDelegate<>(modId, (Registry<T>) Registry.BLOCK);
        }

        if (typeClass == Fluid.class) {
            return new FabricVanillaRegistryRegistrarDelegate<>(modId, (Registry<T>) Registry.FLUID);
        }

        if (typeClass == BlockEntityType.class) {
            return new FabricVanillaRegistryRegistrarDelegate<>(modId, (Registry<T>) Registry.BLOCK_ENTITY_TYPE);
        }

        if (typeClass == MenuType.class) {
            return new FabricVanillaRegistryRegistrarDelegate<>(modId, (Registry<T>) Registry.MENU);
        }

        if (typeClass == RecipeSerializer.class) {
            return new FabricVanillaRegistryRegistrarDelegate<>(modId, (Registry<T>) Registry.RECIPE_SERIALIZER);
        }

        throw new IllegalArgumentException("The registry type class: " + typeClass.getName() + " is not supported.");
    }

    @Override
    public <T extends IChiselsAndBitsRegistryEntry> IChiselsAndBitsRegistry.Builder<T> simpleBuilderFor()
    {
        return CustomRegistryManager.getInstance().createNewSimpleBuilder();
    }
}
