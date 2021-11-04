package mod.chiselsandbits.registrars;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.block.BitStorageBlock;
import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.block.ChiseledPrinterBlock;
import mod.chiselsandbits.block.ModificationTableBlock;
import mod.chiselsandbits.materials.MaterialManager;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.Map;

public final class ModBlocks
{

    private static final IRegistrar<Block> BLOCK_REGISTRAR = IRegistrar.create(Block.class, Constants.MOD_ID);

    public static final Map<Material, IRegistryObject<ChiseledBlock>> MATERIAL_TO_BLOCK_CONVERSIONS = Maps.newHashMap();


    public static final IRegistryObject<BitStorageBlock> BIT_STORAGE = BLOCK_REGISTRAR.register("bit_storage", () -> new BitStorageBlock(BlockBehaviour.Properties.of(Material.METAL)
                                                                                                                                                .strength(1.5F, 6.0F)
                                                                                                                                                .requiresCorrectToolForDrops()
                                                                                                                                                .dynamicShape()
                                                                                                                                                .noOcclusion()
                                                                                                                                                .isValidSpawn((p_test_1_, p_test_2_, p_test_3_, p_test_4_) -> false)
                                                                                                                                                .isRedstoneConductor((p_test_1_, p_test_2_, p_test_3_) -> false)
                                                                                                                                                .isSuffocating((p_test_1_, p_test_2_, p_test_3_) -> false)
                                                                                                                                                .isViewBlocking((p_test_1_, p_test_2_, p_test_3_) -> false)));

    public static final IRegistryObject<ModificationTableBlock> MODIFICATION_TABLE = BLOCK_REGISTRAR.register("modification_table", () -> new ModificationTableBlock(BlockBehaviour.Properties.of(Material.METAL)
                                                                                                                                                .strength(1.5F, 6.0F)
                                                                                                                                                .requiresCorrectToolForDrops()
                                                                                                                                                .dynamicShape()
                                                                                                                                                .noOcclusion()
                                                                                                                                                .isValidSpawn((p_test_1_, p_test_2_, p_test_3_, p_test_4_) -> false)
                                                                                                                                                .isRedstoneConductor((p_test_1_, p_test_2_, p_test_3_) -> false)
                                                                                                                                                .isSuffocating((p_test_1_, p_test_2_, p_test_3_) -> false)
                                                                                                                                                .isViewBlocking((p_test_1_, p_test_2_, p_test_3_) -> false)));

    public static final IRegistryObject<ChiseledPrinterBlock> CHISELED_PRINTER = BLOCK_REGISTRAR.register(
      "chiseled_printer",
      () -> new ChiseledPrinterBlock(BlockBehaviour.Properties.of(Material.METAL)
                                         .strength(1.5F, 6.0F)
                                         .requiresCorrectToolForDrops()
                                         .dynamicShape()
                                         .noOcclusion()
                                         .isValidSpawn((p_test_1_, p_test_2_, p_test_3_, p_test_4_) -> false)
                                         .isRedstoneConductor((p_test_1_, p_test_2_, p_test_3_) -> false)
                                         .isSuffocating((p_test_1_, p_test_2_, p_test_3_) -> false)
                                         .isViewBlocking((p_test_1_, p_test_2_, p_test_3_) -> false))
    );



    private ModBlocks()
    {
        throw new IllegalStateException("Tried to initialize: ModBlocks but this is a Utility class.");
    }


    public static void onModConstruction() {
        MaterialManager.getInstance()
          .getKnownMaterials()
          .forEach((name, material) -> MATERIAL_TO_BLOCK_CONVERSIONS.put(
            material,
            BLOCK_REGISTRAR.register(
              "chiseled" + name,
              () -> new ChiseledBlock(BlockBehaviour.Properties
                                         .of(material)
                                         .strength(1.5f, 6f)
                                         .isViewBlocking((p_test_1_, p_test_2_, p_test_3_) -> false)
                                         .isRedstoneConductor((p_test_1_, p_test_2_, p_test_3_) -> false)
                                         .noOcclusion())
            )
          ));
    }
}
