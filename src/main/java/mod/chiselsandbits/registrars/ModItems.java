package mod.chiselsandbits.registrars;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.item.*;
import mod.chiselsandbits.item.bit.BitItem;
import mod.chiselsandbits.materials.MaterialManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

import static mod.chiselsandbits.registrars.ModItemGroups.CHISELS_AND_BITS;

public final class ModItems
{

    public static final  Map<Material, RegistryObject<ChiseledBlockItem>> MATERIAL_TO_ITEM_CONVERSIONS = Maps.newHashMap();
    private final static DeferredRegister<Item>                           ITEM_REGISTRAR               = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    public static final  RegistryObject<ChiselItem>                       ITEM_CHISEL_STONE            =
      ITEM_REGISTRAR.register("chisel_stone", () -> new ChiselItem(Tiers.STONE, new Item.Properties().tab(CHISELS_AND_BITS).stacksTo(1)));
    public static final  RegistryObject<ChiselItem>                       ITEM_CHISEL_IRON             =
      ITEM_REGISTRAR.register("chisel_iron", () -> new ChiselItem(Tiers.IRON, new Item.Properties().tab(CHISELS_AND_BITS).stacksTo(1)));
    public static final  RegistryObject<ChiselItem>                       ITEM_CHISEL_GOLD             =
      ITEM_REGISTRAR.register("chisel_gold", () -> new ChiselItem(Tiers.GOLD, new Item.Properties().tab(CHISELS_AND_BITS).stacksTo(1)));
    public static final  RegistryObject<ChiselItem>                       ITEM_CHISEL_DIAMOND          =
      ITEM_REGISTRAR.register("chisel_diamond", () -> new ChiselItem(Tiers.DIAMOND, new Item.Properties().tab(CHISELS_AND_BITS).stacksTo(1)));
    public static final  RegistryObject<ChiselItem>                       ITEM_CHISEL_NETHERITE        =
      ITEM_REGISTRAR.register("chisel_netherite", () -> new ChiselItem(Tiers.NETHERITE, new Item.Properties().tab(CHISELS_AND_BITS).stacksTo(1)));
    public static final  RegistryObject<BitItem>             ITEM_BLOCK_BIT   =
      ITEM_REGISTRAR.register("block_bit", () -> new BitItem(new Item.Properties().tab(
        CHISELS_AND_BITS)));
    public static final  RegistryObject<MagnifyingGlassItem> MAGNIFYING_GLASS =
      ITEM_REGISTRAR.register("magnifying_glass", () -> new MagnifyingGlassItem(new Item.Properties().tab(
        CHISELS_AND_BITS)));
    public static final  RegistryObject<BitBagItem>          BIT_BAG_DEFAULT  =
      ITEM_REGISTRAR.register("bit_bag", () -> new BitBagItem(new Item.Properties().tab(
        CHISELS_AND_BITS)));
    public static final  RegistryObject<BitBagItem>          ITEM_BIT_BAG_DYED     =
      ITEM_REGISTRAR.register("bit_bag_dyed", () -> new BitBagItem(new Item.Properties().tab(
        CHISELS_AND_BITS)));
    public static final  RegistryObject<BitStorageBlockItem>
                                                                          BIT_STORAGE                  =
      ITEM_REGISTRAR.register("bit_storage", () -> new BitStorageBlockItem(ModBlocks.BIT_STORAGE.get(), new Item.Properties()
                                                                                                                .tab(CHISELS_AND_BITS)));
    public static final  RegistryObject<BlockItem>
                                                                          MODIFICATION_TABLE                  =
      ITEM_REGISTRAR.register("modification_table", () -> new BlockItem(ModBlocks.MODIFICATION_TABLE.get(), new Item.Properties()
                                                                                                                .tab(CHISELS_AND_BITS)));
    public static final  RegistryObject<MeasuringTapeItem>                MEASURING_TAPE               =
      ITEM_REGISTRAR.register("measuring_tape", () -> new MeasuringTapeItem(new Item.Properties().tab(CHISELS_AND_BITS)));
    public static final  RegistryObject<SingleUsePatternItem>             SINGLE_USE_PATTERN_ITEM      =
      ITEM_REGISTRAR.register("pattern_single_use", () -> new SingleUsePatternItem(new Item.Properties().tab(CHISELS_AND_BITS)));
    public static final  RegistryObject<MultiUsePatternItem>              MULTI_USE_PATTERN_ITEM       =
      ITEM_REGISTRAR.register("pattern_multi_use", () -> new MultiUsePatternItem(new Item.Properties().tab(CHISELS_AND_BITS)));

    public static final RegistryObject<QuillItem> QUILL =
      ITEM_REGISTRAR.register("quill", () -> new QuillItem(new Item.Properties().tab(CHISELS_AND_BITS)));

    public static final RegistryObject<SealantItem> SEALANT_ITEM =
      ITEM_REGISTRAR.register("sealant", () -> new SealantItem(new Item.Properties().tab(CHISELS_AND_BITS)));

    public static final RegistryObject<BlockItem> CHISELED_PRINTER =
        ITEM_REGISTRAR.register("chiseled_printer", () -> new BlockItem(ModBlocks.CHISELED_PRINTER.get(), new Item.Properties().tab(CHISELS_AND_BITS)));

    private ModItems()
    {
        throw new IllegalStateException("Tried to initialize: ModItems but this is a Utility class.");
    }

    public static void onModConstruction()
    {
        ITEM_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());

        MaterialManager.getInstance().getKnownMaterials()
          .forEach((name, material) -> {
              MATERIAL_TO_ITEM_CONVERSIONS.put(
                material,
                ITEM_REGISTRAR.register(
                  "chiseled" + name,
                  () -> new ChiseledBlockItem(
                    ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.get(material).get(),
                    new Item.Properties()
                  )
                )
              );
          });
    }
}
