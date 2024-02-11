package mod.chiselsandbits.recipe.modificationtable;

import com.communi.suggestu.scena.core.registries.ICustomRegistryEntry;
import com.communi.suggestu.scena.core.registries.SimpleCustomRegistryEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ModificationTableRecipeSerializer extends SimpleCustomRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ModificationTableRecipe>
{

    private static final Codec<ModificationTableRecipe> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
        ResourceLocation.CODEC.xmap(
                resourceLocation -> IModificationOperation.getRegistry().get(resourceLocation).orElseThrow(),
                ICustomRegistryEntry::getRegistryName
        ).fieldOf("operation").forGetter(ModificationTableRecipe::getOperation)
      ).apply(instance, ModificationTableRecipe::new)
    );

    @Override
    public @NotNull Codec<ModificationTableRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull ModificationTableRecipe fromNetwork(FriendlyByteBuf pBuffer) {
        final ResourceLocation operation = pBuffer.readResourceLocation();
        return new ModificationTableRecipe(IModificationOperation.getRegistry().get(operation).orElseThrow());
    }

    @Override
    public void toNetwork(final @NotNull FriendlyByteBuf buffer, final @NotNull ModificationTableRecipe recipe)
    {
        buffer.writeResourceLocation(recipe.getOperation().getRegistryName());
    }
}
