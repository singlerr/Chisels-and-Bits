package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;

public final class ModTags
{

    public static void init() {
        Items.init();
        Blocks.init();
    }

    public static final class Items {
        private static void init () {}

        public static Tag.Named<Item> CHISEL = tag("chisel");
        public static Tag.Named<Item> BIT_BAG = tag("bit_bag");

        private static Tag.Named<Item> tag(String name)
        {
            return ItemTags.bind(Constants.MOD_ID + ":" + name);
        }
    }

    public static final class Blocks {
        private static void init() {}

        public static Tag.Named<Block> FORCED_CHISELABLE = tag("chiselable/forced");
        public static Tag.Named<Block> BLOCKED_CHISELABLE = tag("chiselable/blocked");

        //TODO: Implement datagen: ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.values().stream().map(RegistryObject::get).collect(Collectors.toSet())
        public static Tag.Named<Block> CHISELED_BLOCK = tag("chiseled/block");

        private static Tag.Named<Block> tag(String name)
        {
            return BlockTags.bind(Constants.MOD_ID + ":" + name);
        }
    }
}
