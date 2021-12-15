package mod.chiselsandbits.fabric.integration.forge;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

public class ForgeTags
{
    public static void init ()
    {
        Items.init();
    }

    public static class Items
    {
        private static void init(){}

        public static final Tag.Named<Item> FEATHERS = tag("feathers"); //NEEDED IN RECIPES
        public static final Tag.Named<Item> GLASS = tag("glass"); //NEEDED IN RECIPES
        public static final Tag.Named<Item> INGOTS_IRON = tag("ingots/iron"); //NEEDED IN RECIPES
        public static final Tag.Named<Item> NUGGETS_IRON = tag("nuggets/iron"); //NEEDED IN RECIPES
        public static final Tag.Named<Item> RODS_BLAZE = tag("rods/blaze"); //NEEDED IN RECIPES
        public static final Tag.Named<Item> RODS_WOODEN = tag("rods/wooden"); //NEEDED IN RECIPES
        public static final Tag.Named<Item> SLIMEBALLS = tag("slimeballs"); //NEEDED IN RECIPES

        private static Tag.Named<Item> tag(String name)
        {
            return ItemTags.bind("forge:" + name);
        }
    }
}
