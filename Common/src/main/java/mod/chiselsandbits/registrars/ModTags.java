package mod.chiselsandbits.registrars;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ModTags
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static final class Items
    {
        public static Tag.Named<Item> CHISEL  = tag("chisel");
        public static Tag.Named<Item> BIT_BAG = tag("bit_bag");

        public static Tag.Named<Item> FORGE_PAPER = forge("paper");

        private static void init() {}

        private static Tag.Named<Item> tag(String name)
        {
            return ItemTags.bind(Constants.MOD_ID + ":" + name);
        }

        private static Tag.Named<Item> forge(String name)
        {
            return ItemTags.bind( "forge:" + name);
        }
    }

    public static final class Blocks
    {
        public static Tag.Named<Block> FORCED_CHISELABLE  = tag("chiselable/forced");
        public static Tag.Named<Block> BLOCKED_CHISELABLE = tag("chiselable/blocked");
        //TODO: Implement datagen: ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(RegistryObject::get).collect(Collectors.toSet())
        public static Tag.Named<Block> CHISELED_BLOCK     = tag("chiseled/block");

        private static void init() {}

        private static Tag.Named<Block> tag(String name)
        {
            return BlockTags.bind(Constants.MOD_ID + ":" + name);
        }
    }

    public static void onModConstruction()
    {
        Items.init();
        Blocks.init();
        LOGGER.info("Loaded tag configuration.");
    }
}
