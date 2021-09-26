package mod.chiselsandbits.data.recipe;

import com.google.gson.JsonElement;
import mod.chiselsandbits.api.item.documentation.IDocumentableItem;
import mod.chiselsandbits.api.util.ReflectionUtils;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.data.init.GameInitializationManager;
import mod.chiselsandbits.data.recipe.data.ItemWikiDataObject;
import mod.chiselsandbits.data.recipe.data.RecipeObject;
import mod.chiselsandbits.data.recipe.data.RecipesObject;
import mod.chiselsandbits.utils.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WikiRecipesDataProvider implements DataProvider
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new WikiRecipesDataProvider(event.getExistingFileHelper(), event.getGenerator()));
    }

    private final ExistingFileHelper existingFileHelper;
    private final DataGenerator      dataGenerator;

    private WikiRecipesDataProvider(final ExistingFileHelper existingFileHelper, final DataGenerator dataGenerator)
    {
        this.existingFileHelper = existingFileHelper;
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void run(final @NotNull HashCache cache) throws IOException
    {
        GameInitializationManager.getInstance().initialize(existingFileHelper);

        final ExtendedRecipeManager recipeManager = new ExtendedRecipeManager();
        final ResourceManager resourceManager = (ResourceManager) ReflectionUtils.getField(existingFileHelper, "serverData");

        final Map<ResourceLocation, JsonElement> recipeData = recipeManager.prepare(resourceManager, InactiveProfiler.INSTANCE);
        recipeManager.apply(recipeData, resourceManager, InactiveProfiler.INSTANCE);



        for (Item item : ForgeRegistries.ITEMS)
        {
            if (!item.getRegistryName().getNamespace().equals(Constants.MOD_ID))
            {
                continue;
            }

            final ItemWikiDataObject itemWikiDataObject = new ItemWikiDataObject();

            for (CraftingRecipe recipe : recipeManager.getAllRecipesFor(RecipeType.CRAFTING))
            {
                if (recipe.getResultItem().getItem() == item && recipe.getIngredients().size() > 0)
                {
                    final String recipeName = String.format("%s_%s_produces_%s_%s",
                      recipe.getId().getNamespace(),
                      recipe.getId().getPath(),
                      item.getRegistryName().getNamespace(),
                      item.getRegistryName().getPath());

                    final RecipesObject recipesObject = new RecipesObject();
                    final RecipeObject recipeObject = new RecipeObject();
                    recipesObject.getRecipes().add(recipeObject);

                    setRecipeItem(recipe, recipeObject, 0);
                    setRecipeItem(recipe, recipeObject, 1);
                    setRecipeItem(recipe, recipeObject, 2);
                    setRecipeItem(recipe, recipeObject, 3);
                    setRecipeItem(recipe, recipeObject, 4);
                    setRecipeItem(recipe, recipeObject, 5);
                    setRecipeItem(recipe, recipeObject, 6);
                    setRecipeItem(recipe, recipeObject, 7);
                    setRecipeItem(recipe, recipeObject, 8);

                    if (!(item instanceof IDocumentableItem))
                    {
                        recipeObject.getProducts().add(recipe.getResultItem().getItem().getRegistryName().toString().replace(":", "/"));
                    }
                    else
                    {
                        recipeObject.getProducts().addAll(((IDocumentableItem) item).getDocumentableInstances(item).keySet().stream()
                          .map(name -> item.getRegistryName().getNamespace() + "/" + name)
                          .collect(Collectors.toSet())
                        );
                    }
                    recipeObject.setShapeless(!(recipe instanceof IShapedRecipe));

                    itemWikiDataObject.getRecipes().add(recipeName);

                    final Path outputFile = this.dataGenerator.getOutputFolder().resolve("recipes").resolve(recipeName + ".json");
                    DataProvider.save(DataGeneratorConstants.GSONLang, cache, DataGeneratorConstants.GSON.toJsonTree(recipesObject), outputFile);
                }

                if (recipe.getIngredients().size() > 0) {
                    if (recipe.getIngredients().stream().anyMatch(
                      ingredient -> Arrays.stream(ingredient.getItems()).anyMatch(stack -> stack.getItem() == item)
                    )) {
                        final String recipeName = String.format("%s_%s_consumes_%s_%s",
                          recipe.getId().getNamespace(),
                          recipe.getId().getPath(),
                          item.getRegistryName().getNamespace(),
                          item.getRegistryName().getPath());

                        final RecipesObject recipesObject = new RecipesObject();
                        final RecipeObject recipeObject = new RecipeObject();
                        recipesObject.getRecipes().add(recipeObject);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject, 0);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject, 1);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject, 2);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject, 3);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject, 4);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject, 5);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject, 6);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject, 7);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject, 8);

                        if (!(item instanceof IDocumentableItem))
                        {
                            recipeObject.getProducts().add(recipe.getResultItem().getItem().getRegistryName().toString().replace(":", "/"));
                        }
                        else
                        {
                            recipeObject.getProducts().addAll(((IDocumentableItem) item).getDocumentableInstances(item).keySet().stream()
                                                                .map(name -> item.getRegistryName().getNamespace() + "/" + name)
                                                                .collect(Collectors.toSet())
                            );
                        }
                        recipeObject.setShapeless(!(recipe instanceof IShapedRecipe));

                        itemWikiDataObject.getUsages().add(recipeName);

                        final Path outputFile = this.dataGenerator.getOutputFolder().resolve("recipes").resolve(recipeName + ".json");
                        DataProvider.save(DataGeneratorConstants.GSONLang, cache, DataGeneratorConstants.GSON.toJsonTree(recipesObject), outputFile);
                    }
                }
            }

            if (!itemWikiDataObject.getRecipes().isEmpty() || !itemWikiDataObject.getUsages().isEmpty()) {
                final String dataName = String.format("data_%s_%s",
                  item.getRegistryName().getNamespace(),
                  item.getRegistryName().getPath());

                final Path outputFile = this.dataGenerator.getOutputFolder().resolve("recipes").resolve(dataName + ".json");
                DataProvider.save(DataGeneratorConstants.GSONLang, cache, DataGeneratorConstants.GSON.toJsonTree(itemWikiDataObject), outputFile);
            }
        }
    }

    private void setRecipeItem(final CraftingRecipe recipe, final RecipeObject recipeObject, int index) {
        final int recipeWidth = recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getWidth() : 3;
        final int recipeHeight = recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getHeight() : 3;

        final List<String> target = recipeObject.getItemFromIndex(index, recipeWidth, recipeHeight);
        if (recipe.getIngredients().size() > index) {
            target.addAll(
              Arrays.stream(recipe.getIngredients().get(index).getItems())
                .map(ItemStack::getItem)
                .flatMap(stackItem -> {
                    if (!(stackItem instanceof IDocumentableItem))
                        return Stream.of(stackItem.getRegistryName().toString().replace(":", "/"));

                    return ((IDocumentableItem) stackItem).getDocumentableInstances(stackItem).keySet().stream()
                      .map(name -> stackItem.getRegistryName().getNamespace() + "/" + name);
                })
                .collect(Collectors.toSet())
            );
        }
    }

    private void setRecipeItemWhenNotSource(final Item item, final CraftingRecipe recipe, final RecipeObject recipeObject, final int index)
    {
        final int recipeWidth = recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getWidth() : 3;
        final int recipeHeight = recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getHeight() : 3;

        final List<String> target = recipeObject.getItemFromIndex(index, recipeWidth, recipeHeight);

        if (recipe.getIngredients().size() > index)
        {
            if (!Arrays.stream(recipe.getIngredients().get(index).getItems()).anyMatch(s -> s.getItem() == item))
            {
                target.addAll(
                  Arrays.stream(recipe.getIngredients().get(index).getItems())
                    .map(ItemStack::getItem)
                    .map(Item::getRegistryName)
                    .map(ResourceLocation::toString)
                    .map(name -> name.replace(":", "/"))
                    .collect(Collectors.toSet())
                );
            }
            else
            {
                if (!(item instanceof IDocumentableItem))
                {
                    target.add(item.getRegistryName().toString().replace(":", "/"));
                }
                else
                {
                    target.addAll(((IDocumentableItem) item).getDocumentableInstances(item).keySet().stream()
                                                        .map(name -> item.getRegistryName().getNamespace() + "/" + name)
                                                        .collect(Collectors.toSet())
                    );
                }
            }
        }
    }

    @Override
    public @NotNull String getName()
    {
        return "Wiki recipes generator";
    }
}
