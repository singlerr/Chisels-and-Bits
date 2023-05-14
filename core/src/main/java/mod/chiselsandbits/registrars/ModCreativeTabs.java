package mod.chiselsandbits.registrars;

import com.communi.suggestu.scena.core.creativetab.ICreativeTabManager;
import com.communi.suggestu.scena.core.registries.IPlatformRegistryManager;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.client.clipboard.ICreativeClipboardManager;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.api.variant.state.IStateVariantManager;
import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.item.BitBagItem;
import mod.chiselsandbits.item.bit.BitItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class ModCreativeTabs
{
    public static Supplier<CreativeModeTab> MAIN;
    public static Supplier<CreativeModeTab> BITS;
    public static Supplier<CreativeModeTab> CLIPBOARD;

    private static final Logger          LOGGER           = LogManager.getLogger();

    private ModCreativeTabs()
    {
        throw new IllegalStateException("Tried to initialize: ModItemGroups but this is a Utility class.");
    }

    public static void onModConstruction()
    {
        LOGGER.info("Loaded item group configuration.");

        MAIN = ICreativeTabManager.getInstance().register(
                builder -> {
                    builder.icon(() -> new ItemStack(ModItems.ITEM_CHISEL_NETHERITE.get()));
                    builder.title(LocalStrings.ChiselsAndBitsName.getText());
                    builder.displayItems((parameters, output) -> {
                        output.accept(new ItemStack(ModItems.ITEM_CHISEL_STONE.get()));
                        output.accept(new ItemStack(ModItems.ITEM_CHISEL_IRON.get()));
                        output.accept(new ItemStack(ModItems.ITEM_CHISEL_GOLD.get()));
                        output.accept(new ItemStack(ModItems.ITEM_CHISEL_DIAMOND.get()));
                        output.accept(new ItemStack(ModItems.ITEM_CHISEL_NETHERITE.get()));
                        output.accept(new ItemStack(ModItems.ITEM_BIT_BAG_DEFAULT.get()));
                        for (DyeColor color : DyeColor.values())
                        {
                            output.accept(BitBagItem.dyeBag(new ItemStack(ModItems.ITEM_BIT_BAG_DYED.get()), color));
                        }
                        output.accept(new ItemStack(ModItems.MAGNIFYING_GLASS.get()));
                        output.accept(new ItemStack(ModItems.ITEM_BIT_STORAGE.get()));
                        output.accept(new ItemStack(ModItems.ITEM_MODIFICATION_TABLE.get()));
                        output.accept(new ItemStack(ModItems.MEASURING_TAPE.get()));
                        output.accept(new ItemStack(ModItems.SINGLE_USE_PATTERN_ITEM.get()));
                        output.accept(new ItemStack(ModItems.MULTI_USE_PATTERN_ITEM.get()));
                        output.accept(new ItemStack(ModItems.QUILL.get()));
                        output.accept(new ItemStack(ModItems.SEALANT_ITEM.get()));
                        output.accept(new ItemStack(ModItems.CHISELED_PRINTER.get()));
                        output.accept(new ItemStack(ModItems.MONOCLE_ITEM.get()));
                    });
                },
                new ResourceLocation(Constants.MOD_ID, "main"),
                List.of(new ResourceLocation("spawn_eggs")),
                List.of()
        );
        BITS = ICreativeTabManager.getInstance().register(
                builder -> {
                    builder.icon(() -> new ItemStack(ModItems.ITEM_BLOCK_BIT.get()));
                    builder.title(LocalStrings.CreativeTabBits.getText());
                    builder.type(CreativeModeTab.Type.SEARCH);
                    builder.displayItems((parameters, output) -> IPlatformRegistryManager.getInstance().getBlockRegistry().getValues()
                            .forEach(block -> {
                                if (block instanceof ChiseledBlock)
                                    return;

                                final BlockState blockState = block.defaultBlockState();
                                final Collection<IBlockInformation> defaultStateVariants = IStateVariantManager.getInstance().getAllDefaultVariants(blockState);

                                if (!defaultStateVariants.isEmpty()) {
                                    defaultStateVariants.forEach(blockInformation -> {
                                        final ItemStack resultStack = IBitItemManager.getInstance().create(blockInformation);

                                        if (!resultStack.isEmpty() && resultStack.getItem() instanceof IBitItem)
                                            output.accept(resultStack);
                                    });
                                    return;
                                }

                                final BlockInformation information = new BlockInformation(blockState, Optional.empty());

                                if (IEligibilityManager.getInstance().canBeChiseled(information)) {
                                    final ItemStack resultStack = IBitItemManager.getInstance().create(information);

                                    if (!resultStack.isEmpty() && resultStack.getItem() instanceof IBitItem) {
                                        output.accept(resultStack);
                                    }
                                }
                            }));
                },
                new ResourceLocation(Constants.MOD_ID, "bits"),
                List.of(new ResourceLocation(Constants.MOD_ID, "main")),
                List.of()
        );
        CLIPBOARD = ICreativeTabManager.getInstance().register(
                builder -> {
                    builder.icon(() -> new ItemStack(ModItems.PATTERN_SCANNER.get()));
                    builder.title(LocalStrings.CreativeTabClipboard.getText());
                    builder.displayItems((parameters, output) -> output.acceptAll(
                            ICreativeClipboardManager.getInstance().getClipboard()
                                    .stream()
                                    .map(IMultiStateItemStack::toBlockStack)
                                    .toList()
                    ));
                },
                new ResourceLocation(Constants.MOD_ID, "clipboard"),
                List.of(new ResourceLocation(Constants.MOD_ID, "bits")),
                List.of()
        );
    }
}
