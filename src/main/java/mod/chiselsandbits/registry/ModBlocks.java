package mod.chiselsandbits.registry;

import com.google.common.collect.Maps;
import mod.chiselsandbits.bittank.BlockBitTank;
import mod.chiselsandbits.bittank.ItemBlockBitTank;
import mod.chiselsandbits.bittank.ItemStackSpecialRendererBitTank;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.MaterialType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.station.ChiselStationBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static mod.chiselsandbits.registry.ModItemGroups.CHISELS_AND_BITS;

public final class ModBlocks
{

    private static final DeferredRegister<Block> BLOCK_REGISTRAR = DeferredRegister.create(ForgeRegistries.BLOCKS, ChiselsAndBits.MODID);
    private static final DeferredRegister<Item> ITEM_REGISTRAR = DeferredRegister.create(ForgeRegistries.ITEMS, ChiselsAndBits.MODID);

    private static final Map<Material, RegistryObject<BlockChiseled>> MATERIAL_TO_BLOCK_CONVERSIONS = Maps.newHashMap();
    private static final Map<Material, RegistryObject<ItemBlockChiseled>>  MATERIAL_TO_ITEM_CONVERSIONS = Maps.newHashMap();

    public static final RegistryObject<BlockBitTank> BIT_TANK_BLOCK = BLOCK_REGISTRAR.register("bit_tank", () -> new BlockBitTank(AbstractBlock.Properties.create(Material.IRON)
                                                                                                                                    .hardnessAndResistance(1.5F, 6.0F)
                                                                                                                                    .harvestTool(ToolType.AXE)
                                                                                                                                    .harvestLevel(1)
                                                                                                                                    .setRequiresTool()
                                                                                                                                    .variableOpacity()
                                                                                                                                    .notSolid()
                                                                                                                                    .setAllowsSpawn((p_test_1_, p_test_2_, p_test_3_, p_test_4_) -> false)
                                                                                                                                    .setOpaque((p_test_1_, p_test_2_, p_test_3_) -> false)
                                                                                                                                    .setSuffocates((p_test_1_, p_test_2_, p_test_3_) -> false)
                                                                                                                                    .setBlocksVision((p_test_1_, p_test_2_, p_test_3_) -> false)));

    public static final RegistryObject<BlockItem> BIT_TANK_BLOCK_ITEM = ITEM_REGISTRAR.register("bit_tank", () -> new ItemBlockBitTank(BIT_TANK_BLOCK.get(), new Item.Properties()
                                                                                                                                                               .group(CHISELS_AND_BITS)
                                                                                                                                                               .setISTER(() -> ItemStackSpecialRendererBitTank::new)));
    public static final RegistryObject<ChiselStationBlock> CHISEL_STATION_BLOCK = BLOCK_REGISTRAR.register("chisel_station", () -> new ChiselStationBlock(AbstractBlock.Properties.create(Material.ROCK)
      .hardnessAndResistance(1.5f, 6f)
      .harvestLevel(1)
      .harvestTool(ToolType.PICKAXE)
      .notSolid()
      .setOpaque((p_test_1_, p_test_2_, p_test_3_) -> false)
      .setBlocksVision((p_test_1_, p_test_2_, p_test_3_) -> false)
    ));

    public static final RegistryObject<BlockItem> CHISEL_STATION_ITEM = ITEM_REGISTRAR.register("chisel_station", () -> new BlockItem(ModBlocks.CHISEL_STATION_BLOCK.get(), new Item.Properties().group(CHISELS_AND_BITS)));

    private static final MaterialType[] VALID_CHISEL_MATERIALS = new MaterialType[] {
      new MaterialType( "wood", Material.WOOD ),
      new MaterialType( "rock", Material.ROCK ),
      new MaterialType( "iron", Material.IRON ),
      new MaterialType( "cloth", Material.CARPET ),
      new MaterialType( "ice", Material.ICE ),
      new MaterialType( "packed_ice", Material.PACKED_ICE ),
      new MaterialType( "clay", Material.CLAY ),
      new MaterialType( "glass", Material.GLASS ),
      new MaterialType( "sand", Material.SAND ),
      new MaterialType( "ground", Material.EARTH ),
      new MaterialType( "grass", Material.EARTH ),
      new MaterialType( "snow", Material.SNOW_BLOCK ),
      new MaterialType( "fluid", Material.WATER ),
      new MaterialType( "leaves", Material.LEAVES ),
    };

    private ModBlocks()
    {
        throw new IllegalStateException("Tried to initialize: ModBlocks but this is a Utility class.");
    }

    public static void onModConstruction() {
        BLOCK_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEM_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());

        Arrays.stream(VALID_CHISEL_MATERIALS).forEach(materialType -> {
            MATERIAL_TO_BLOCK_CONVERSIONS.put(
              materialType.getType(),
              BLOCK_REGISTRAR.register("chiseled" + materialType.getName(), () -> new BlockChiseled("chiseled_" + materialType.getName(), AbstractBlock.Properties
                                                                                                                                            .create(materialType.getType())
                                                                                                                                            .hardnessAndResistance(1.5f, 6f)
                                                                                                                                            .setBlocksVision((p_test_1_, p_test_2_, p_test_3_) -> false)
                                                                                                                                            .setOpaque((p_test_1_, p_test_2_, p_test_3_) -> false)
                                                                                                                                            .notSolid()))
            );
            MATERIAL_TO_ITEM_CONVERSIONS.put(
              materialType.getType(),
              ITEM_REGISTRAR.register("chiseled" + materialType.getName(), () -> new ItemBlockChiseled(MATERIAL_TO_BLOCK_CONVERSIONS.get(materialType.getType()).get(), new Item.Properties()))
            );
          }
        );
    }

    public static Map<Material, RegistryObject<ItemBlockChiseled>> getMaterialToItemConversions()
    {
        return MATERIAL_TO_ITEM_CONVERSIONS;
    }

    public static Map<Material, RegistryObject<BlockChiseled>> getMaterialToBlockConversions()
    {
        return MATERIAL_TO_BLOCK_CONVERSIONS;
    }

    public static MaterialType[] getValidChiselMaterials()
    {
        return VALID_CHISEL_MATERIALS;
    }

    @Nullable
    public static BlockState getChiseledDefaultState() {
        final Iterator<RegistryObject<BlockChiseled>> blockIterator = getMaterialToBlockConversions().values().iterator();
        if (blockIterator.hasNext())
            return blockIterator.next().get().getDefaultState();

        return null;
    }

    public static BlockChiseled convertGivenStateToChiseledBlock(
      final BlockState state )
    {
        final Fluid f = BlockBitInfo.getFluidFromBlock( state.getBlock() );
        return convertGivenMaterialToChiseledBlock(f != null ? Material.WATER : state.getMaterial());
    }

    public static BlockChiseled convertGivenMaterialToChiseledBlock(
      final Material material
    ) {
        final RegistryObject<BlockChiseled> materialBlock = getMaterialToBlockConversions().get( material );
        return materialBlock != null ? materialBlock.get() : convertGivenMaterialToChiseledBlock(Material.ROCK);
    }

    public static RegistryObject<BlockChiseled> convertGivenStateToChiseledRegistryBlock(
      final BlockState state )
    {
        final Fluid f = BlockBitInfo.getFluidFromBlock( state.getBlock() );
        return convertGivenMaterialToChiseledRegistryBlock(f != null ? Material.WATER : state.getMaterial());
    }

    public static RegistryObject<BlockChiseled> convertGivenMaterialToChiseledRegistryBlock(
      final Material material
    ) {
        final RegistryObject<BlockChiseled> materialBlock = getMaterialToBlockConversions().get( material );
        return materialBlock != null ? materialBlock : convertGivenMaterialToChiseledRegistryBlock(Material.ROCK);
    }

    public static boolean convertMaterialTo(
      final Material source,
      final Material target
    ) {
        final RegistryObject<BlockChiseled> sourceRegisteredObject = convertGivenMaterialToChiseledRegistryBlock(source);
        return getMaterialToBlockConversions().put(target, sourceRegisteredObject) != null;
    }
}
