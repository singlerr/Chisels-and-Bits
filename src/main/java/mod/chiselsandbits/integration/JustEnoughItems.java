package mod.chiselsandbits.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IRecipeRegistration;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static mod.chiselsandbits.registry.ModItems.*;

@JeiPlugin
public class JustEnoughItems implements IModPlugin
{
    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {

        final ArrayList<Item> blocks = new ArrayList<>();
        for (final RegistryObject<ItemBlockChiseled> blk : ModBlocks.getMaterialToItemConversions().values())
        {
            blocks.add(blk.get());
        }

        addDescription(
          registration,
          LocalStrings.LongHelpChisel,
          ITEM_CHISEL_DIAMOND.get(),
          ITEM_CHISEL_GOLD.get(),
          ITEM_CHISEL_IRON.get(),
          ITEM_CHISEL_STONE.get());

        addDescription(
          registration,
          LocalStrings.LongHelpChiseledBlock,
          blocks.toArray(new Item[0])
        );

        addDescription(registration, LocalStrings.LongHelpBitBag, ITEM_BIT_BAG_DEFAULT.get(), ITEM_BIT_BAG_DYED.get());
        addDescription(registration, LocalStrings.LongHelpBit, ITEM_BLOCK_BIT.get());
        addDescription(registration, LocalStrings.LongHelpMirrorPrint, ITEM_MIRROR_PRINT.get(), ITEM_MIRROR_PRINT_WRITTEN.get());
        addDescription(registration, LocalStrings.LongHelpNegativePrint, ITEM_NEGATIVE_PRINT.get(), ITEM_NEGATIVE_PRINT_WRITTEN.get());
        addDescription(registration, LocalStrings.LongHelpPositivePrint, ITEM_POSITIVE_PRINT.get(), ITEM_POSITIVE_PRINT_WRITTEN.get());
        addDescription(registration, LocalStrings.LongHelpBitSaw, ITEM_BIT_SAW_DIAMOND.get());
        addDescription(registration, LocalStrings.LongHelpTapeMeasure, ITEM_TAPE_MEASURE.get());
        addDescription(registration, LocalStrings.LongHelpWrench, ITEM_WRENCH.get());
        addDescription(registration, LocalStrings.LongHelpBitTank, ModBlocks.BIT_TANK_BLOCK_ITEM.get());
        addDescription(registration, LocalStrings.LongHelpMagnifyingGlass, ITEM_MAGNIFYING_GLASS.get());

        //addDescription(registration, LocalStrings.LongChiselStationHelp, ModBlocks.CHISEL_STATION_ITEM.get());
    }

    private void addDescription(
      final IRecipeRegistration registry,
      final LocalStrings local,
      final Item... items)
    {
        if (items.length > 0)
        {
            final List<ItemStack> stacks = Arrays.asList(items).stream().map(ItemStack::new).collect(Collectors.toList());
            final IIngredientType<ItemStack> itemStackIIngredientType = registry.getIngredientManager().getIngredientType(ItemStack.class);
            registry.addIngredientInfo(stacks, itemStackIIngredientType, local.toString());
        }
    }

    @Override
    public ResourceLocation getPluginUid()
    {
        return new ResourceLocation(Constants.MOD_ID, "jei");
    }
}

