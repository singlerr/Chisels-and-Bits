package mod.chiselsandbits.registrars;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.block.entities.ChiseledPrinterBlockEntity;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("ConstantConditions")
public final class ModBlockEntityTypes
{
    private static final Logger                         LOGGER    = LogManager.getLogger();
    private static final IRegistrar<BlockEntityType<?>> REGISTRAR = IRegistrar.create(BlockEntityType.class, Constants.MOD_ID);

    private ModBlockEntityTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModTileEntityTypes but this is a Utility class.");
    }

    public static void onModConstruction()
    {
        LOGGER.info("Loaded block entity configuration.");
    }

    public static IRegistryObject<BlockEntityType<ChiseledBlockEntity>> CHISELED = REGISTRAR.register("chiseled", () -> BlockEntityType.Builder.of(
        ChiseledBlockEntity::new,
        ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(IRegistryObject::get).toArray(Block[]::new)
      ).build(null)
    );

    public static IRegistryObject<BlockEntityType<BitStorageBlockEntity>> BIT_STORAGE = REGISTRAR.register("bit_storage", () -> BlockEntityType.Builder.of(
        BitStorageBlockEntity::new,
        ModBlocks.BIT_STORAGE.get()
      ).build(null)
    );

    public static final IRegistryObject<BlockEntityType<ChiseledPrinterBlockEntity>> CHISELED_PRINTER = REGISTRAR.register(
      "chiseled_printer",
      () -> BlockEntityType.Builder.of(
        ChiseledPrinterBlockEntity::new,
        ModBlocks.CHISELED_PRINTER.get()
      ).build(null)
    );
}