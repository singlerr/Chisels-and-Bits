package mod.chiselsandbits.fabric.integration.forge;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
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

        public static final TagKey<Item>    FEATHERS = tag("feathers"); //NEEDED IN RECIPES
        public static final TagKey<Item> GLASS    = tag("glass"); //NEEDED IN RECIPES
        public static final TagKey<Item> INGOTS_IRON = tag("ingots/iron"); //NEEDED IN RECIPES
        public static final TagKey<Item> NUGGETS_IRON = tag("nuggets/iron"); //NEEDED IN RECIPES
        public static final TagKey<Item> RODS_BLAZE = tag("rods/blaze"); //NEEDED IN RECIPES
        public static final TagKey<Item> RODS_WOODEN = tag("rods/wooden"); //NEEDED IN RECIPES
        public static final TagKey<Item> SLIMEBALLS = tag("slimeballs"); //NEEDED IN RECIPES

        private static TagKey<Item> tag(String name)
        {
            return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge:" + name));
        }
    }
}
