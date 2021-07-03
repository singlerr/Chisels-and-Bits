package mod.chiselsandbits.recipe;

import mod.chiselsandbits.item.BitBagItem;
import mod.chiselsandbits.registrars.ModItems;
import mod.chiselsandbits.registrars.ModRecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class BagDyeingRecipe extends SpecialRecipe
{

    public BagDyeingRecipe(
      ResourceLocation name)
    {
        super(name);
    }

    @Override
    public boolean matches(final @NotNull CraftingInventory inv, final @NotNull World worldIn)
    {
        return !getOutput(inv).getBag().isEmpty();
    }

    @Override
    public @NotNull ItemStack getCraftingResult(
      @NotNull CraftingInventory inv)
    {
        Result output = getOutput(inv);

        if (!output.getBag().isEmpty())
        {
            return BitBagItem.dyeBag(output.bag, output.color);
        }

        return ItemStack.EMPTY;
    }

    @NotNull
    private Result getOutput(
      CraftingInventory inv)
    {
        ItemStack bag = null;
        ItemStack dye = null;

        for (int x = 0; x < inv.getSizeInventory(); ++x)
        {
            ItemStack is = inv.getStackInSlot(x);
            if (!is.isEmpty())
            {
                if (is.getItem() == Items.WATER_BUCKET || getDye(is) != null)
                {
                    if (dye == null)
                    {
                        dye = is;
                    }
                    else
                    {
                        return Result.EMPTY;
                    }
                }
                else if (is.getItem() instanceof BitBagItem)
                {
                    if (bag == null)
                    {
                        bag = is;
                    }
                    else
                    {
                        return Result.EMPTY;
                    }
                }
                else
                {
                    return Result.EMPTY;
                }
            }
        }

        if (bag != null && dye != null)
        {
            return new Result(bag, getDye(dye));
        }

        return Result.EMPTY;
    }

    private DyeColor getDye(
      ItemStack is)
    {
        if (Tags.Items.DYES_WHITE.contains(is.getItem()))
            return DyeColor.WHITE;
        if (Tags.Items.DYES_ORANGE.contains(is.getItem()))
            return DyeColor.ORANGE;
        if (Tags.Items.DYES_MAGENTA.contains(is.getItem()))
            return DyeColor.MAGENTA;
        if (Tags.Items.DYES_LIGHT_BLUE.contains(is.getItem()))
            return DyeColor.LIGHT_BLUE;
        if (Tags.Items.DYES_YELLOW.contains(is.getItem()))
            return DyeColor.YELLOW;
        if (Tags.Items.DYES_LIME.contains(is.getItem()))
            return DyeColor.LIME;
        if (Tags.Items.DYES_PINK.contains(is.getItem()))
            return DyeColor.PINK;
        if (Tags.Items.DYES_GRAY.contains(is.getItem()))
            return DyeColor.GRAY;
        if (Tags.Items.DYES_LIGHT_GRAY.contains(is.getItem()))
            return DyeColor.LIGHT_GRAY;
        if (Tags.Items.DYES_CYAN.contains(is.getItem()))
            return DyeColor.CYAN;
        if (Tags.Items.DYES_PURPLE.contains(is.getItem()))
            return DyeColor.PURPLE;
        if (Tags.Items.DYES_BLUE.contains(is.getItem()))
            return DyeColor.BLUE;
        if (Tags.Items.DYES_BROWN.contains(is.getItem()))
            return DyeColor.BROWN;
        if (Tags.Items.DYES_GREEN.contains(is.getItem()))
            return DyeColor.GREEN;
        if (Tags.Items.DYES_RED.contains(is.getItem()))
            return DyeColor.RED;
        if (Tags.Items.DYES_BLACK.contains(is.getItem()))
            return DyeColor.BLACK;
        
        if (is.getItem() instanceof DyeItem)
        {
            final DyeItem item = (DyeItem) is.getItem();
            return item.getDyeColor();
        }

        return null;
    }

    @Override
    public boolean canFit(final int width, final int height)
    {
        return width * height >= 2;
    }

    @Override
    public @NotNull IRecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.BAG_DYEING.get();
    }

    private static class Result
    {
        private static final Result EMPTY = new Result(
          ItemStack.EMPTY,
          DyeColor.WHITE
        );

        private final ItemStack bag;
        private final DyeColor  color;

        public Result(
          ItemStack bag,
          DyeColor dye)
        {
            this.bag = bag;
            this.color = dye;
        }

        public ItemStack getBag()
        {
            return bag;
        }

        public DyeColor getColor()
        {
            return color;
        }
    }
}
