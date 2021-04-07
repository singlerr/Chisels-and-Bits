package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("ConstantConditions")
public final class ModTileEntityTypes
{

    private static final DeferredRegister<TileEntityType<?>> REGISTRAR = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Constants.MOD_ID);

    private ModTileEntityTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModTileEntityTypes but this is a Utility class.");
    }

    public static RegistryObject<TileEntityType<ChiseledBlockEntity>> CHISELED = REGISTRAR.register("chiseled", () -> TileEntityType.Builder.create(
      ChiseledBlockEntity::new,
      ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(RegistryObject::get).toArray(Block[]::new)
      ).build(null)
    );

    public static void onModConstruction()
    {
        REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
