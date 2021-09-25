package mod.chiselsandbits.registrars;

import mod.chiselsandbits.aabb.AABBManager;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.block.entities.ChiseledPrinterBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("ConstantConditions")
public final class ModTileEntityTypes
{


    private static final DeferredRegister<TileEntityType<?>>                       REGISTRAR        = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Constants.MOD_ID);

    private ModTileEntityTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModTileEntityTypes but this is a Utility class.");
    }

    public static RegistryObject<TileEntityType<ChiseledBlockEntity>> CHISELED = REGISTRAR.register("chiseled", () -> TileEntityType.Builder.of(
      ChiseledBlockEntity::new,
      ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(RegistryObject::get).toArray(Block[]::new)
      ).build(null)
    );

    public static RegistryObject<TileEntityType<BitStorageBlockEntity>> BIT_STORAGE = REGISTRAR.register("bit_storage", () -> TileEntityType.Builder.of(
      BitStorageBlockEntity::new,
      ModBlocks.BIT_STORAGE.get()
      ).build(null)
    );

    public static final RegistryObject<TileEntityType<ChiseledPrinterBlockEntity>> CHISELED_PRINTER = REGISTRAR.register(
      "chiseled_printer",
      () -> TileEntityType.Builder.of(
        ChiseledPrinterBlockEntity::new,
        ModBlocks.CHISELED_PRINTER.get()
      ).build(null)
    );

    public static void onModConstruction()
    {
        REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
