package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.block.entities.ChiseledPrinterBlockEntity;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

@SuppressWarnings("ConstantConditions")
public final class ModTileEntityTypes
{


    private static final IRegistrar<BlockEntityType<?>> REGISTRAR = IRegistrar.create(BlockEntityType.class, Constants.MOD_ID);

    private ModTileEntityTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModTileEntityTypes but this is a Utility class.");
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