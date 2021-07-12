package mod.chiselsandbits.data.recipe;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import mod.chiselsandbits.api.item.documentation.IDocumentableItem;
import mod.chiselsandbits.api.util.ReflectionUtils;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.data.icons.RenderedItemModelDataProvider;
import mod.chiselsandbits.data.init.AsyncReloadManager;
import mod.chiselsandbits.data.init.GameInitializationManager;
import mod.chiselsandbits.data.recipe.data.ItemWikiDataObject;
import mod.chiselsandbits.data.recipe.data.RecipeObject;
import mod.chiselsandbits.data.recipe.data.RecipesObject;
import mod.chiselsandbits.utils.DataGeneratorConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tags.TagRegistryManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WikiRecipesDataProvider implements IDataProvider
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
    public void act(final @NotNull DirectoryCache cache) throws IOException
    {
        GameInitializationManager.getInstance().initialize(existingFileHelper);

        final ExtendedRecipeManager recipeManager = new ExtendedRecipeManager();
        final IResourceManager resourceManager = (IResourceManager) ReflectionUtils.getField(existingFileHelper, "serverData");

        final Map<ResourceLocation, JsonElement> recipeData = recipeManager.prepare(resourceManager, EmptyProfiler.INSTANCE);
        recipeManager.apply(recipeData, resourceManager, EmptyProfiler.INSTANCE);



        for (Item item : ForgeRegistries.ITEMS)
        {
            if (!item.getRegistryName().getNamespace().equals(Constants.MOD_ID))
            {
                continue;
            }

            final ItemWikiDataObject itemWikiDataObject = new ItemWikiDataObject();

            for (ICraftingRecipe recipe : recipeManager.getRecipesForType(IRecipeType.CRAFTING))
            {
                if (recipe.getRecipeOutput().getItem() == item && recipe.getIngredients().size() > 0)
                {
                    final String recipeName = String.format("%s_%s_produces_%s_%s",
                      recipe.getId().getNamespace(),
                      recipe.getId().getPath(),
                      item.getRegistryName().getNamespace(),
                      item.getRegistryName().getPath());

                    final RecipesObject recipesObject = new RecipesObject();
                    final RecipeObject recipeObject = new RecipeObject();
                    recipesObject.getRecipes().add(recipeObject);

                    recipeObject.getFirstRow().getFirstItem().addAll(
                      Arrays.stream(recipe.getIngredients().get(0).getMatchingStacks())
                        .map(ItemStack::getItem)
                        .map(Item::getRegistryName)
                        .map(ResourceLocation::toString)
                        .map(name -> name.replace(":", "/"))
                        .collect(Collectors.toSet())
                    );

                    if (recipe.getIngredients().size() > 1)
                    {
                        recipeObject.getFirstRow().getSecondItem().addAll(
                          Arrays.stream(recipe.getIngredients().get(1).getMatchingStacks())
                            .map(ItemStack::getItem)
                            .map(Item::getRegistryName)
                            .map(ResourceLocation::toString)
                            .map(name -> name.replace(":", "/"))
                            .collect(Collectors.toSet())
                        );
                    }

                    if (recipe.getIngredients().size() > 2)
                    {
                        recipeObject.getFirstRow().getThirdItem().addAll(
                          Arrays.stream(recipe.getIngredients().get(2).getMatchingStacks())
                            .map(ItemStack::getItem)
                            .map(Item::getRegistryName)
                            .map(ResourceLocation::toString)
                            .map(name -> name.replace(":", "/"))
                            .collect(Collectors.toSet())
                        );
                    }

                    if (recipe.getIngredients().size() > 3)
                    {
                        recipeObject.getSecondRow().getFirstItem().addAll(
                          Arrays.stream(recipe.getIngredients().get(3).getMatchingStacks())
                            .map(ItemStack::getItem)
                            .map(Item::getRegistryName)
                            .map(ResourceLocation::toString)
                            .map(name -> name.replace(":", "/"))
                            .collect(Collectors.toSet())
                        );
                    }

                    if (recipe.getIngredients().size() > 4)
                    {
                        recipeObject.getSecondRow().getSecondItem().addAll(
                          Arrays.stream(recipe.getIngredients().get(4).getMatchingStacks())
                            .map(ItemStack::getItem)
                            .map(Item::getRegistryName)
                            .map(ResourceLocation::toString)
                            .map(name -> name.replace(":", "/"))
                            .collect(Collectors.toSet())
                        );
                    }

                    if (recipe.getIngredients().size() > 5)
                    {
                        recipeObject.getSecondRow().getThirdItem().addAll(
                          Arrays.stream(recipe.getIngredients().get(5).getMatchingStacks())
                            .map(ItemStack::getItem)
                            .map(Item::getRegistryName)
                            .map(ResourceLocation::toString)
                            .map(name -> name.replace(":", "/"))
                            .collect(Collectors.toSet())
                        );
                    }

                    if (recipe.getIngredients().size() > 6)
                    {
                        recipeObject.getThirdRow().getFirstItem().addAll(
                          Arrays.stream(recipe.getIngredients().get(6).getMatchingStacks())
                            .map(ItemStack::getItem)
                            .map(Item::getRegistryName)
                            .map(ResourceLocation::toString)
                            .map(name -> name.replace(":", "/"))
                            .collect(Collectors.toSet())
                        );
                    }

                    if (recipe.getIngredients().size() > 7)
                    {
                        recipeObject.getThirdRow().getSecondItem().addAll(
                          Arrays.stream(recipe.getIngredients().get(7).getMatchingStacks())
                            .map(ItemStack::getItem)
                            .map(Item::getRegistryName)
                            .map(ResourceLocation::toString)
                            .map(name -> name.replace(":", "/"))
                            .collect(Collectors.toSet())
                        );
                    }

                    if (recipe.getIngredients().size() > 8)
                    {
                        recipeObject.getThirdRow().getThirdItem().addAll(
                          Arrays.stream(recipe.getIngredients().get(8).getMatchingStacks())
                            .map(ItemStack::getItem)
                            .map(Item::getRegistryName)
                            .map(ResourceLocation::toString)
                            .map(name -> name.replace(":", "/"))
                            .collect(Collectors.toSet())
                        );
                    }

                    if (!(item instanceof IDocumentableItem))
                    {
                        recipeObject.getProducts().add(recipe.getRecipeOutput().getItem().getRegistryName().toString().replace(":", "/"));
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
                    IDataProvider.save(DataGeneratorConstants.GSONLang, cache, DataGeneratorConstants.GSON.toJsonTree(recipesObject), outputFile);
                }

                if (recipe.getIngredients().size() > 0) {
                    if (recipe.getIngredients().stream().anyMatch(
                      ingredient -> Arrays.stream(ingredient.getMatchingStacks()).anyMatch(stack -> stack.getItem() == item)
                    )) {
                        final String recipeName = String.format("%s_%s_consumes_%s_%s",
                          recipe.getId().getNamespace(),
                          recipe.getId().getPath(),
                          item.getRegistryName().getNamespace(),
                          item.getRegistryName().getPath());

                        final RecipesObject recipesObject = new RecipesObject();
                        final RecipeObject recipeObject = new RecipeObject();
                        recipesObject.getRecipes().add(recipeObject);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject.getFirstRow().getFirstItem(), 0);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject.getFirstRow().getSecondItem(), 1);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject.getFirstRow().getThirdItem(), 2);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject.getSecondRow().getFirstItem(), 3);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject.getSecondRow().getSecondItem(), 4);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject.getSecondRow().getThirdItem(), 5);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject.getThirdRow().getFirstItem(), 6);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject.getThirdRow().getSecondItem(), 7);

                        setRecipeItemWhenNotSource(item, recipe, recipeObject.getThirdRow().getThirdItem(), 8);

                        if (!(item instanceof IDocumentableItem))
                        {
                            recipeObject.getProducts().add(recipe.getRecipeOutput().getItem().getRegistryName().toString().replace(":", "/"));
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
                        IDataProvider.save(DataGeneratorConstants.GSONLang, cache, DataGeneratorConstants.GSON.toJsonTree(recipesObject), outputFile);
                    }
                }
            }

            if (!itemWikiDataObject.getRecipes().isEmpty() || !itemWikiDataObject.getUsages().isEmpty()) {
                final String dataName = String.format("data_%s_%s",
                  item.getRegistryName().getNamespace(),
                  item.getRegistryName().getPath());

                final Path outputFile = this.dataGenerator.getOutputFolder().resolve("recipes").resolve(dataName + ".json");
                IDataProvider.save(DataGeneratorConstants.GSONLang, cache, DataGeneratorConstants.GSON.toJsonTree(itemWikiDataObject), outputFile);
            }
        }
    }

    private void setRecipeItemWhenNotSource(final Item item, final ICraftingRecipe recipe, final List<String> target, final int index)
    {
        if (recipe.getIngredients().size() > index)
        {
            if (Arrays.stream(recipe.getIngredients().get(index).getMatchingStacks()).anyMatch(s -> s.getItem() == item))
            {
                target.addAll(
                  Arrays.stream(recipe.getIngredients().get(index).getMatchingStacks())
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
                    target.add(recipe.getRecipeOutput().getItem().getRegistryName().toString().replace(":", "/"));
                }
                else
                {
                    target.addAll(((IDocumentableItem) item).getDocumentableInstances(item).keySet().stream()
                                                        .map(name -> item.getRegistryName().getNamespace() + "/" + name)
                                                        .collect(Collectors.toSet())
                    );
                }

                target.add(item.getRegistryName().toString().replace(":", "/"));
            }
        }
    }

    @Override
    public @NotNull String getName()
    {
        return "Wiki recipes generator";
    }
}
