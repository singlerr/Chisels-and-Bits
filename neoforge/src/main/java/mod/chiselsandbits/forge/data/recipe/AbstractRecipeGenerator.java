package mod.chiselsandbits.forge.data.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public abstract class AbstractRecipeGenerator extends RecipeProvider
{
    private final ItemLike itemProvider;

    public AbstractRecipeGenerator(PackOutput packOutput, ItemLike itemProvider) {
        super(packOutput);
        this.itemProvider = itemProvider;
    }


    public ItemLike getItemProvider()
    {
        return itemProvider;
    }

    @Override
    public final @NotNull String getName()
    {
        return Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(itemProvider.asItem())) + " recipe generator";
    }
}