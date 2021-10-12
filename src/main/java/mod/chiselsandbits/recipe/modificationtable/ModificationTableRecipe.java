package mod.chiselsandbits.recipe.modificationtable;

import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.item.pattern.IMultiUsePatternItem;
import mod.chiselsandbits.api.item.pattern.IPatternItem;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.multistate.snapshot.EmptySnapshot;
import mod.chiselsandbits.registrars.ModRecipeSerializers;
import mod.chiselsandbits.registrars.ModRecipeTypes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ModificationTableRecipe implements IRecipe<IInventory>
{
    private final IModificationOperation operation;

    public ModificationTableRecipe(final IModificationOperation operation) {this.operation = operation;}

    public IModificationOperation getOperation()
    {
        return operation;
    }

    @Override
    public boolean matches(final IInventory inv, final @NotNull World worldIn)
    {
        return inv.getItem(0).getItem() instanceof IPatternItem && !(inv.getItem(0).getItem() instanceof IMultiUsePatternItem);
    }

    @Override
    public @NotNull ItemStack assemble(final @NotNull IInventory inv)
    {
        return getAppliedSnapshot(inv).toItemStack().toPatternStack();
    }

    public @NotNull ItemStack getCraftingBlockResult(final IInventory inv)
    {
        return getAppliedSnapshot(inv).toItemStack().toBlockStack();
    }

    public @NotNull IMultiStateSnapshot getAppliedSnapshot(final IInventory inv)
    {
        final ItemStack multiStateStack = inv.getItem(0);
        if (multiStateStack.isEmpty())
            return EmptySnapshot.INSTANCE;

        if (!(multiStateStack.getItem() instanceof IMultiStateItem))
            return EmptySnapshot.INSTANCE;

        final IMultiStateItem item = (IMultiStateItem) multiStateStack.getItem();
        final IMultiStateItemStack multiStateItemStack = item.createItemStack(multiStateStack);
        final IMultiStateSnapshot snapshot = multiStateItemStack.createSnapshot().clone();

        getOperation().apply(snapshot);

        return snapshot;
    }

    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(
          Objects.requireNonNull(this.getOperation().getRegistryName()).getNamespace() + ".recipes.chisel.pattern.modification." + this.getOperation().getRegistryName().getPath());
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 0;
    }

    @Override
    public @NotNull ItemStack getResultItem()
    {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ResourceLocation getId()
    {
        return Objects.requireNonNull(getOperation().getRegistryName());
    }

    @Override
    public @NotNull IRecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.MODIFICATION_TABLE.get();
    }

    @Override
    public @NotNull IRecipeType<?> getType()
    {
        return ModRecipeTypes.MODIFICATION_TABLE;
    }
}
