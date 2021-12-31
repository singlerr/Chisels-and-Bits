package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.clipboard.ICreativeClipboardManager;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.platforms.core.creativetab.ICreativeTabManager;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public final class ModCreativeTabs
{
    public static final  CreativeModeTab CHISELS_AND_BITS = ICreativeTabManager.getInstance().register((index) -> new CreativeModeTab(index, Constants.MOD_ID)
    {
        @Override
        public @NotNull ItemStack makeIcon()
        {
            return new ItemStack(ModItems.ITEM_CHISEL_STONE.get());
        }
    });

    public static final  CreativeModeTab CLIPBOARD = ICreativeTabManager.getInstance().register((index) -> new CreativeModeTab(index, Constants.MOD_ID)
    {
        @Override
        public @NotNull ItemStack makeIcon()
        {
            return new ItemStack(ModItems.PATTERN_SCANNER.get());
        }

        @Override
        public Component getDisplayName()
        {
            return LocalStrings.CreativeTabClipboard.getText();
        }

        @Override
        public void fillItemList(final @NotNull NonNullList<ItemStack> stacksToFill)
        {
            stacksToFill.addAll(
              ICreativeClipboardManager.getInstance().getClipboard()
                .stream()
                .map(IMultiStateItemStack::toBlockStack)
                .toList()
            );
        }
    });
    private static final Logger          LOGGER           = LogManager.getLogger();

    private ModCreativeTabs()
    {
        throw new IllegalStateException("Tried to initialize: ModItemGroups but this is a Utility class.");
    }

    public static void onModConstruction()
    {
        LOGGER.info("Loaded item group configuration.");
    }

}
