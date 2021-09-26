package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.block.entities.ChiseledPrinterBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("ConstantConditions")
public final class ModTileEntityTypes
{


    private static final DeferredRegister<BlockEntityType<?>>                       REGISTRAR        = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Constants.MOD_ID);

    private ModTileEntityTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModTileEntityTypes but this is a Utility class.");
    }

    public static RegistryObject<BlockEntityType<ChiseledBlockEntity>> CHISELED = REGISTRAR.register("chiseled", () -> BlockEntityType.Builder.of(
      ChiseledBlockEntity::new,
      ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(RegistryObject::get).toArray(Block[]::new)
      ).build(null)
    );

    public static RegistryObject<BlockEntityType<BitStorageBlockEntity>> BIT_STORAGE = REGISTRAR.register("bit_storage", () -> BlockEntityType.Builder.of(
      BitStorageBlockEntity::new,
      ModBlocks.BIT_STORAGE.get()
      ).build(null)
    );

    public static final RegistryObject<BlockEntityType<ChiseledPrinterBlockEntity>> CHISELED_PRINTER = REGISTRAR.register(
      "chiseled_printer",
      () -> BlockEntityType.Builder.of(
        ChiseledPrinterBlockEntity::new,
        ModBlocks.CHISELED_PRINTER.get()
      ).build(null)
    );

    public static void onModConstruction()
    {
        REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
