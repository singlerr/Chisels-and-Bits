package mod.chiselsandbits.forge.data.recipe;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public abstract class AbstractRecipeGenerator extends RecipeProvider
{
    private final ItemLike itemProvider;

    protected AbstractRecipeGenerator(final DataGenerator generator, final ItemLike itemProvider) {
        super(generator);
        this.itemProvider = itemProvider;
    }

    public ItemLike getItemProvider()
    {
        return itemProvider;
    }

    @Override
    public final @NotNull String getName()
    {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemProvider.asItem())) + " recipe generator";
    }
}