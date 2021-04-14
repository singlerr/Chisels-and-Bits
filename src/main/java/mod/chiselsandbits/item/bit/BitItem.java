package mod.chiselsandbits.item.bit;

import com.google.common.collect.Lists;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.chiseling.IChiselMode;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.item.chisel.IChiselingItem;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class BitItem extends Item implements IChiselingItem, IBitItem
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<ItemStack> availableBitStacks = Lists.newLinkedList();

    public BitItem(final Properties properties)
    {
        super(properties);
    }

    @Override
    public ClickProcessingState handleLeftClickProcessing(
      final PlayerEntity playerEntity, final Hand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        return handleClickProcessing(
          playerEntity, hand, currentState, IChiselMode::onLeftClickBy
        );
    }

    @NotNull
    @Override
    public IChiselMode getMode(final ItemStack stack)
    {
        final CompoundNBT stackNbt = stack.getOrCreateTag();
        if (stackNbt.contains(NbtConstants.CHISEL_MODE))
        {
            final String chiselModeName = stackNbt.getString(NbtConstants.CHISEL_MODE);
            try {
                final IChiselMode registryMode = IChiselMode.getRegistry().getValue(new ResourceLocation(chiselModeName));
                if (registryMode == null)
                {
                    return IChiselMode.getDefaultMode();
                }

                return registryMode;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error(String.format("An ItemStack got loaded with a name that is not a valid chisel mode: %s", chiselModeName));
                this.setMode(stack, IChiselMode.getDefaultMode());
            }
        }

        return IChiselMode.getDefaultMode();
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final ItemStack stack)
    {
        final BlockState containedStack = getBitState(stack);
        final Block block = containedStack.getBlock();

        return new TranslationTextComponent(this.getTranslationKey(stack), block.asItem().getDisplayName(new ItemStack(block)));
    }

    @Override
    public void addInformation(
      @NotNull final ItemStack stack, @Nullable final World worldIn, final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        tooltip.add(TranslationUtils.build("chiselmode.mode", getMode(stack).getDisplayName()));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public void setMode(final ItemStack stack, final IChiselMode mode)
    {
        stack.getOrCreateTag().putString(NbtConstants.CHISEL_MODE, Objects.requireNonNull(mode.getRegistryName()).toString());
    }

    @NotNull
    @Override
    public Collection<IChiselMode> getPossibleModes()
    {
        return IChiselMode.getRegistry().getValues();
    }

    @Override
    public ClickProcessingState handleRightClickProcessing(
      final PlayerEntity playerEntity, final Hand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        return handleClickProcessing(
          playerEntity, hand, currentState, IChiselMode::onRightClickBy
        );
    }

    private ClickProcessingState handleClickProcessing(
      final PlayerEntity playerEntity, final Hand hand, final ClickProcessingState currentState, final ChiselModeInteractionCallback callback)
    {
        final ItemStack itemStack = playerEntity.getHeldItem(hand);
        if (itemStack.isEmpty() || itemStack.getItem() != this)
            return currentState;

        final IChiselingItem chiselingItem = (IChiselingItem) itemStack.getItem();
        final IChiselMode chiselMode = chiselingItem.getMode(itemStack);

        final IChiselingContext context = IChiselingManager.getInstance().getOrCreateContext(
          playerEntity,
          chiselMode
        );

        return callback.run(chiselMode, playerEntity, context);
    }

    @Override
    public BlockState getBitState(final ItemStack stack)
    {
        return NBTUtil.readBlockState(stack.getOrCreateChildTag(NbtConstants.BLOCK_STATE));
    }

    @FunctionalInterface
    private interface ChiselModeInteractionCallback {
        ClickProcessingState run(final IChiselMode chiselMode, final PlayerEntity playerEntity, final IChiselingContext context);
    }

    @Override
    public void fillItemGroup(@Nullable final ItemGroup group, @NotNull final NonNullList<ItemStack> items)
    {
        if (group == null || this.getGroup() != group) {
            return;
        }

        if (availableBitStacks.isEmpty()) {
            ForgeRegistries.BLOCKS.getValues()
              .forEach(block -> {
                  if (IEligibilityManager.getInstance().canBeChiseled(block)) {
                    final BlockState blockState = block.getDefaultState();
                    final ItemStack resultStack = IBitItemManager.getInstance().create(blockState);

                    if (!resultStack.isEmpty() && resultStack.getItem() instanceof IBitItem)
                        this.availableBitStacks.add(resultStack);
                  }
              });

            availableBitStacks.sort(Comparator.comparing(stack -> {
                if (!(stack.getItem() instanceof IBitItem))
                    throw new IllegalStateException("Stack did not contain a bit item.");

                return IBlockStateIdManager.getInstance().getIdFrom(((IBitItem) stack.getItem()).getBitState(stack));
            }));
        }

        items.addAll(availableBitStacks);
    }
}
