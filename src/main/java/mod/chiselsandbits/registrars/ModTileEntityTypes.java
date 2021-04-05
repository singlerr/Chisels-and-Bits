package mod.chiselsandbits.registrars;

import mod.chiselsandbits.bitstorage.TileEntityBitStorage;
import mod.chiselsandbits.legacy.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.printer.ChiselPrinterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModTileEntityTypes
{

    private static final DeferredRegister<TileEntityType<?>> REGISTRAR = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ChiselsAndBits.MODID);

    private ModTileEntityTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModTileEntityTypes but this is a Utility class.");
    }

    public static RegistryObject<TileEntityType<TileEntityBlockChiseled>> CHISELED = REGISTRAR.register("chiseled", () -> TileEntityType.Builder.create(
      TileEntityBlockChiseled::new,
      ModBlocks.getMaterialToBlockConversions().values().stream().map(RegistryObject::get).toArray(Block[]::new)
      ).build(null)
    );

    public static RegistryObject<TileEntityType<TileEntityBitStorage>> BIT_STORAGE = REGISTRAR.register("bit_storage", () -> TileEntityType.Builder.create(
      TileEntityBitStorage::new,
      ModBlocks.BIT_STORAGE_BLOCK.get()
      ).build(null)
    );

    public static RegistryObject<TileEntityType<ChiselPrinterTileEntity>> CHISEL_PRINTER = REGISTRAR.register("chisel_printer", () -> TileEntityType.Builder.create(
      ChiselPrinterTileEntity::new,
      ModBlocks.CHISEL_PRINTER_BLOCK.get()
      ).build(null)
    );

    public static void onModConstruction()
    {
        REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
