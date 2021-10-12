package mod.chiselsandbits.recipe.modificationtable;

import com.google.gson.JsonObject;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModificationTableRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ModificationTableRecipe>
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
    public ModificationTableRecipe fromNetwork(final @NotNull ResourceLocation recipeId, final @NotNull PacketBuffer buffer)
    {
        return new ModificationTableRecipe(IChiselsAndBitsAPI.getInstance().getRegistryManager().getModificationOperationRegistry().getValue(recipeId));
    }

    @Override
    public void toNetwork(final @NotNull PacketBuffer buffer, final @NotNull ModificationTableRecipe recipe)
    {
    }
}
