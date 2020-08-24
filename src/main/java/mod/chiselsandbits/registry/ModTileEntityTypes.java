package mod.chiselsandbits.registry;

import mod.chiselsandbits.bittank.TileEntityBitTank;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.DistExecutor;
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

    public static RegistryObject<TileEntityType<TileEntityBlockChiseled>> CHISELED_TESR = REGISTRAR.register("chiseled_tesr", () -> TileEntityType.Builder.create(
      () -> DistExecutor.unsafeRunForDist(
        () -> () -> new TileEntityBlockChiseledTESR(),
        () -> () -> new TileEntityBlockChiseled.TileEntityBlockChiseledDummy()
      ),
      ModBlocks.getMaterialToBlockConversions().values().stream().map(RegistryObject::get).toArray(Block[]::new)
      ).build(null)
    );

    public static RegistryObject<TileEntityType<TileEntityBitTank>> BIT_TANK = REGISTRAR.register("bit_tank", () -> TileEntityType.Builder.create(
      () -> new TileEntityBitTank(),
      ModBlocks.BIT_TANK_BLOCK.get()
      ).build(null)
    );

    public static void onModConstruction()
    {
        REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
