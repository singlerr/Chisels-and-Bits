package mod.chiselsandbits.registry;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.*;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mod.chiselsandbits.registry.ModItemGroups.CHISELS_AND_BITS;

public final class ModItems
{

    private final static DeferredRegister<Item> ITEM_REGISTRAR = DeferredRegister.create(ForgeRegistries.ITEMS, ChiselsAndBits.MODID);

    private ModItems()
    {
        throw new IllegalStateException("Tried to initialize: ModItems but this is a Utility class.");
    }

    public static final RegistryObject<ItemChisel> ITEM_CHISEL_STONE   =
      ITEM_REGISTRAR.register("chisel_stone", () -> new ItemChisel(ItemTier.STONE, new Item.Properties().group(
      CHISELS_AND_BITS)));
    public static final RegistryObject<ItemChisel> ITEM_CHISEL_IRON    =
      ITEM_REGISTRAR.register("chisel_iron", () -> new ItemChisel(ItemTier.IRON, new Item.Properties().group(
      CHISELS_AND_BITS)));
    public static final RegistryObject<ItemChisel> ITEM_CHISEL_GOLD    =
      ITEM_REGISTRAR.register("chisel_gold", () -> new ItemChisel(ItemTier.GOLD, new Item.Properties().group(
      CHISELS_AND_BITS)));
    public static final RegistryObject<ItemChisel> ITEM_CHISEL_DIAMOND =
      ITEM_REGISTRAR.register("chisel_diamond", () -> new ItemChisel(ItemTier.DIAMOND, new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemChisel> ITEM_CHISEL_NETHERITE =
      ITEM_REGISTRAR.register("chisel_netherite", () -> new ItemChisel(ItemTier.NETHERITE, new Item.Properties().group(
        CHISELS_AND_BITS).isImmuneToFire()));

    public static final RegistryObject<ItemChiseledBit>   ITEM_BLOCK_BIT              =
      ITEM_REGISTRAR.register("block_bit", () -> new ItemChiseledBit(new Item.Properties().group(
      CHISELS_AND_BITS)));
    public static final RegistryObject<ItemMirrorPrint>   ITEM_MIRROR_PRINT           =
      ITEM_REGISTRAR.register("mirrorprint", () -> new ItemMirrorPrint(new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemMirrorPrint>   ITEM_MIRROR_PRINT_WRITTEN   =
      ITEM_REGISTRAR.register("mirrorprint_written", () -> new ItemMirrorPrint(new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemPositivePrint> ITEM_POSITIVE_PRINT         =
      ITEM_REGISTRAR.register("positiveprint", () -> new ItemPositivePrint(new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemPositivePrint> ITEM_POSITIVE_PRINT_WRITTEN =
      ITEM_REGISTRAR.register("positiveprint_written", () -> new ItemPositivePrint(new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemNegativePrint> ITEM_NEGATIVE_PRINT         =
      ITEM_REGISTRAR.register("negativeprint", () -> new ItemNegativePrint(new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemNegativePrint> ITEM_NEGATIVE_PRINT_WRITTEN =
      ITEM_REGISTRAR.register("negativeprint_written", () -> new ItemNegativePrint(new Item.Properties().group(
        CHISELS_AND_BITS)));

    public static final RegistryObject<ItemBitBag>      ITEM_BIT_BAG_DEFAULT =
      ITEM_REGISTRAR.register("bit_bag", () -> new ItemBitBag(new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemBitBag>      ITEM_BIT_BAG_DYED =
      ITEM_REGISTRAR.register("bit_bag_dyed", () -> new ItemBitBag(new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemWrench>      ITEM_WRENCH =
      ITEM_REGISTRAR.register("wrench_wood", () -> new ItemWrench(new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemBitSaw>      ITEM_BIT_SAW_STONE =
      ITEM_REGISTRAR.register("bitsaw_stone", () -> new ItemBitSaw(ItemTier.STONE, new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemBitSaw>      ITEM_BIT_SAW_GOLD =
      ITEM_REGISTRAR.register("bitsaw_gold", () -> new ItemBitSaw(ItemTier.GOLD, new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemBitSaw>      ITEM_BIT_SAW_IRON =
      ITEM_REGISTRAR.register("bitsaw_iron", () -> new ItemBitSaw(ItemTier.IRON, new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemBitSaw>      ITEM_BIT_SAW_DIAMOND =
      ITEM_REGISTRAR.register("bitsaw_diamond", () -> new ItemBitSaw(ItemTier.DIAMOND, new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemBitSaw>      ITEM_BIT_SAW_NETHERITE =
      ITEM_REGISTRAR.register("bitsaw_netherite", () -> new ItemBitSaw(ItemTier.NETHERITE, new Item.Properties().group(
        CHISELS_AND_BITS).isImmuneToFire()));
    public static final RegistryObject<ItemTapeMeasure> ITEM_TAPE_MEASURE =
      ITEM_REGISTRAR.register("tape_measure", () -> new ItemTapeMeasure(new Item.Properties().group(
        CHISELS_AND_BITS)));
    public static final RegistryObject<ItemMagnifyingGlass>      ITEM_MAGNIFYING_GLASS =
      ITEM_REGISTRAR.register("magnifying_glass", () -> new ItemMagnifyingGlass(new Item.Properties().group(
        CHISELS_AND_BITS)));

    public static void onModConstruction() {
        ITEM_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
