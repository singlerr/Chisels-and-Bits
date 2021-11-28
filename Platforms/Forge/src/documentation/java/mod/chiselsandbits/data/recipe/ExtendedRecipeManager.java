package mod.chiselsandbits.data.recipe;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ExtendedRecipeManager extends RecipeManager
{

    @Override
    public void apply(
      final @NotNull Map<ResourceLocation, JsonElement> objectIn, final @NotNull ResourceManager resourceManagerIn, final @NotNull ProfilerFiller profilerIn)
    {
        super.apply(objectIn, resourceManagerIn, profilerIn);
    }

    @Override
    public @NotNull Map<ResourceLocation, JsonElement> prepare(
      final @NotNull ResourceManager resourceManagerIn, final @NotNull ProfilerFiller profilerIn)
    {
        return super.prepare(resourceManagerIn, profilerIn);
    }
}
