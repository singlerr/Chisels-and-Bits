package mod.chiselsandbits.data.recipe;

import com.google.gson.JsonElement;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.data.icons.RenderedItemModelDataProvider;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ExtendedRecipeManager extends RecipeManager
{

    @Override
    public void apply(
      final @NotNull Map<ResourceLocation, JsonElement> objectIn, final @NotNull IResourceManager resourceManagerIn, final @NotNull IProfiler profilerIn)
    {
        super.apply(objectIn, resourceManagerIn, profilerIn);
    }

    @Override
    public @NotNull Map<ResourceLocation, JsonElement> prepare(
      final @NotNull IResourceManager resourceManagerIn, final @NotNull IProfiler profilerIn)
    {
        return super.prepare(resourceManagerIn, profilerIn);
    }
}
