package mod.chiselsandbits.data.recipe;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import com.ldtteam.datagenerators.recipes.RecipeResultJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedPatternJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedRecipeJson;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.registry.ModTags;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BitTankRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new BitTankRecipeGenerator(event.getGenerator()));
    }

    private BitTankRecipeGenerator(final DataGenerator generator) {
        super(generator, ModBlocks.BIT_TANK_BLOCK_ITEM.get());
    }

    @Override
    protected void generate() throws IOException
    {
        addShapedRecipe(
          "igi",
          "glg",
          "ici",
          "g",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.GLASS.getName().toString(), true)),
          "l",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(ItemTags.LOGS.getName().toString(), true)),
          "i",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.INGOTS_IRON.getName().toString(), true)),
          "c",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(ModTags.Items.CHISEL.getName().toString(), true))
        );
    }
}
