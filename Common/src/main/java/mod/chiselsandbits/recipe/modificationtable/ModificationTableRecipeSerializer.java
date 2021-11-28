package mod.chiselsandbits.recipe.modificationtable;

import com.google.gson.JsonObject;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.platforms.core.registries.SimpleChiselsAndBitsRegistryEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ModificationTableRecipeSerializer extends SimpleChiselsAndBitsRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ModificationTableRecipe>
{
    @Override
    public @NotNull ModificationTableRecipe fromJson(final @NotNull ResourceLocation recipeId, final @NotNull JsonObject json)
    {
        return IChiselsAndBitsAPI.getInstance().getRegistryManager().getModificationOperationRegistry().get(recipeId)
                 .map(ModificationTableRecipe::new)
                 .orElseThrow(() -> new IllegalArgumentException(String.format("No modification table recipe is known for the id: %s", recipeId)));
    }

    @Override
    public @NotNull ModificationTableRecipe fromNetwork(final @NotNull ResourceLocation recipeId, final @NotNull FriendlyByteBuf buffer)
    {
        return IChiselsAndBitsAPI.getInstance().getRegistryManager().getModificationOperationRegistry().get(recipeId)
          .map(ModificationTableRecipe::new)
          .orElseThrow(() -> new IllegalArgumentException(String.format("No modification table recipe is known for the id: %s", recipeId)));
    }

    @Override
    public void toNetwork(final @NotNull FriendlyByteBuf buffer, final @NotNull ModificationTableRecipe recipe)
    {
    }

    @SuppressWarnings({"unchecked"})
    public Class<RecipeSerializer<?>> getRegistryType() {
        return (Class<RecipeSerializer<?>>) (Object) RecipeSerializer.class;
    }
}
