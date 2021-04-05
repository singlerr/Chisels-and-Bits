package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.item.ChiselItem;
import mod.chiselsandbits.item.MagnifyingGlassItem;
import mod.chiselsandbits.legacy.items.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mod.chiselsandbits.registrars.ModItemGroups.CHISELS_AND_BITS;

public final class ModItems
{

    private final static DeferredRegister<Item> ITEM_REGISTRAR = DeferredRegister.create(ForgeRegistries.ITEMS, ChiselsAndBits.MODID);

    private ModItems()
    {
        throw new IllegalStateException("Tried to initialize: ModItems but this is a Utility class.");
    }

    public static final RegistryObject<ChiselItem> ITEM_CHISEL_STONE =
      ITEM_REGISTRAR.register("chisel_stone", () -> new ChiselItem(ItemTier.STONE, new Item.Properties().group(CHISELS_AND_BITS)));

    public static final RegistryObject<ItemChiseledBit>   ITEM_BLOCK_BIT              =
      ITEM_REGISTRAR.register("block_bit", () -> new ItemChiseledBit(new Item.Properties().group(
      CHISELS_AND_BITS)));

    public static final RegistryObject<MagnifyingGlassItem> ITEM_MAGNIFYING_GLASS =
      ITEM_REGISTRAR.register("magnifying_glass", () -> new MagnifyingGlassItem(new Item.Properties().group(
        CHISELS_AND_BITS)));

    public static void onModConstruction() {
        ITEM_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
