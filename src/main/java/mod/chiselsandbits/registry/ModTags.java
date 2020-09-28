package mod.chiselsandbits.registry;

import mod.chiselsandbits.utils.Constants;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;

public class ModTags
{

    public static void init() {
        Items.init();
        Blocks.init();
    }

    public static final class Items {
        private static void init () {}

        public static ITag.INamedTag<Item> CHISEL = tag("chisel");
        public static ITag.INamedTag<Item> BIT_BAG = tag("bit_bag");

        private static ITag.INamedTag<Item> tag(String name)
        {
            return ItemTags.makeWrapperTag(Constants.MOD_ID + ":" + name);
        }
    }

    public static final class Blocks {
        private static void init() {}

        public static ITag.INamedTag<Block> FORCED_CHISELABLE = tag("chiselable/forced");
        public static ITag.INamedTag<Block> BLOCKED_CHISELABLE = tag("chiselable/blocked");

        private static ITag.INamedTag<Block> tag(String name)
        {
            return BlockTags.makeWrapperTag(Constants.MOD_ID + ":" + name);
        }
    }
}
