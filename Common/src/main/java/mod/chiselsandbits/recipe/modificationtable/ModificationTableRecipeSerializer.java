package mod.chiselsandbits.recipe.modificationtable;

import com.google.gson.JsonObject;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModificationTableRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ModificationTableRecipe>
{
    @Override
    public @NotNull ModificationTableRecipe fromJson(final @NotNull ResourceLocation recipeId, final @NotNull JsonObject json)
    {
        if (!IChiselsAndBitsAPI.getInstance().getRegistryManager().getModificationOperationRegistry().containsKey(recipeId))
            throw new IllegalArgumentException(String.format("No modification table recipe is known for the id: %s", recipeId));

        return new ModificationTableRecipe(IChiselsAndBitsAPI.getInstance().getRegistryManager().getModificationOperationRegistry().getValue(recipeId));
    }

    @Nullable
    @Override
    public ModificationTableRecipe fromNetwork(final @NotNull ResourceLocation recipeId, final @NotNull FriendlyByteBuf buffer)
    {
        return new ModificationTableRecipe(IChiselsAndBitsAPI.getInstance().getRegistryManager().getModificationOperationRegistry().getValue(recipeId));
    }

    @Override
    public void toNetwork(final @NotNull FriendlyByteBuf buffer, final @NotNull ModificationTableRecipe recipe)
    {
    }
}
