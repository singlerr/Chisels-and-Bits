package mod.chiselsandbits.block.entities;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.item.chisel.IChiselItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.item.pattern.IMultiUsePatternItem;
import mod.chiselsandbits.api.item.pattern.IPatternItem;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.block.ChiseledPrinterBlock;
import mod.chiselsandbits.container.ChiseledPrinterContainer;
import mod.chiselsandbits.platforms.core.item.IItemComparisonHelper;
import mod.chiselsandbits.registrars.ModBlockEntityTypes;
import mod.chiselsandbits.utils.container.SimpleContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ChiseledPrinterBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer
{
    private final   MutableObject<ItemStack>  currentRealisedWorkingStack = new MutableObject<>(ItemStack.EMPTY);
    private final   Optional<SimpleContainer> tool_handler                = Optional.of(new SimpleContainer(1));
    private final   Optional<SimpleContainer> pattern_handler             = Optional.of(new SimpleContainer(1));
    private final   Optional<SimpleContainer> result_handler              = Optional.of(new SimpleContainer(1));
    private         int                       progress                    = 0;
    protected final ContainerData             stationData                 = new ContainerData()
    {
        public int get(int index)
        {
            if (index == 0)
            {
                return ChiseledPrinterBlockEntity.this.progress;
            }
            return 0;
        }

        public void set(int index, int value)
        {
            if (index == 0)
            {
                ChiseledPrinterBlockEntity.this.progress = value;
            }
        }

        public int getCount()
        {
            return 1;
        }
    };
    private         long                      lastTickTime                = 0L;

    public ChiseledPrinterBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntityTypes.CHISELED_PRINTER.get(), pos, state);
    }

    @Override
    public void load(final @NotNull CompoundTag nbt)
    {
        super.load(nbt);

        tool_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("tool")));
        pattern_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("pattern")));
        result_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("result")));

        progress = nbt.getInt("progress");
    }

    @Override
    public void saveAdditional(final @NotNull CompoundTag compound)
    {
        tool_handler.ifPresent(h -> compound.put("tool", h.serializeNBT()));
        pattern_handler.ifPresent(h -> compound.put("pattern", h.serializeNBT()));
        result_handler.ifPresent(h -> compound.put("result", h.serializeNBT()));

        compound.putInt("progress", progress);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag()
    {
        return saveWithFullMetadata();
    }

    public void tick()
    {
        if (getLevel() == null || lastTickTime == getLevel().getGameTime() || getLevel().isClientSide())
        {
            return;
        }

        this.lastTickTime = getLevel().getGameTime();

        if (couldWork())
        {
            if (canWork())
            {
                progress++;
                if (progress >= 100)
                {
                    result_handler.ifPresent(h -> h.setItem(0, realisePattern(true)));
                    currentRealisedWorkingStack.setValue(ItemStack.EMPTY);
                    progress = 0;
                    damageChisel();
                }
                setChanged();
            }
        }
        else if (progress != 0)
        {
            progress = 0;
            setChanged();
        }
    }

    public boolean hasPatternStack()
    {
        return !getPatternStack().isEmpty();
    }

    public boolean hasToolStack()
    {
        return !getToolStack().isEmpty();
    }

    public boolean hasRealisedStack()
    {
        return !getRealisedStack().isEmpty();
    }

    public boolean hasOutputStack()
    {
        return !getOutputStack().isEmpty();
    }

    public boolean canMergeOutputs()
    {
        if (!hasOutputStack())
        {
            return true;
        }

        if (!hasRealisedStack())
        {
            return false;
        }

        return IItemComparisonHelper.getInstance().canItemStacksStack(getOutputStack(), getRealisedStack());
    }

    public boolean canWork()
    {
        return hasPatternStack() && hasToolStack() && canMergeOutputs() && !getRealisedStack().isEmpty();
    }

    public boolean couldWork()
    {
        return hasPatternStack() && hasToolStack();
    }

    public ItemStack getRealisedStack()
    {
        ItemStack realisedStack = currentRealisedWorkingStack.getValue();
        if (realisedStack.isEmpty())
        {
            realisedStack = realisePattern(false);
            currentRealisedWorkingStack.setValue(realisedStack);
        }

        return realisedStack;
    }

    private ItemStack realisePattern(final boolean consumeResources)
    {
        if (!hasPatternStack())
        {
            return ItemStack.EMPTY;
        }

        final ItemStack stack = getPatternStack();
        if (!(stack.getItem() instanceof final IPatternItem patternItem))
        {
            return ItemStack.EMPTY;
        }

        final IMultiStateItemStack realisedPattern = patternItem.createItemStack(stack);
        if (realisedPattern.getStatistics().isEmpty())
        {
            return ItemStack.EMPTY;
        }

        final BlockInformation firstState = getPrimaryBlockState() == null ? BlockInformation.AIR : getPrimaryBlockState();
        final BlockInformation secondState = getSecondaryBlockState() == null ?BlockInformation.AIR : getSecondaryBlockState();
        final BlockInformation thirdState = getTertiaryBlockState() == null ? BlockInformation.AIR : getTertiaryBlockState();

        if (firstState.isAir() && secondState.isAir() && thirdState.isAir())
        {
            return ItemStack.EMPTY;
        }

        if ((!IEligibilityManager.getInstance().canBeChiseled(firstState.getBlockState()) && !firstState.isAir())
              || (!IEligibilityManager.getInstance().canBeChiseled(secondState.getBlockState()) && !secondState.isAir())
              || (!IEligibilityManager.getInstance().canBeChiseled(thirdState.getBlockState()) && !thirdState.isAir())
        )
        {
            return ItemStack.EMPTY;
        }

        final IMultiStateSnapshot modifiableSnapshot = realisedPattern.createSnapshot();
        modifiableSnapshot.mutableStream()
          .filter(e -> (!e.getBlockInformation().equals(firstState) || firstState.equals(BlockInformation.AIR)) && (!e.getBlockInformation().equals(secondState)
                                                                                                          || secondState.equals(BlockInformation.AIR)) && (
            !e.getBlockInformation().equals(thirdState) || thirdState.equals(BlockInformation.AIR)) && !e.getBlockInformation().equals(BlockInformation.AIR))
          .forEach(IMutableStateEntryInfo::clear);

        if (modifiableSnapshot.getStatics().getStateCounts().getOrDefault(firstState, 0) == 0 &&
              modifiableSnapshot.getStatics().getStateCounts().getOrDefault(secondState, 0) == 0 &&
              modifiableSnapshot.getStatics().getStateCounts().getOrDefault(thirdState, 0) == 0
        )
        {
            return ItemStack.EMPTY;
        }

        if ((modifiableSnapshot.getStatics().getStateCounts().getOrDefault(firstState, 0) > getAvailablePrimaryBlockState() && !firstState.equals(BlockInformation.AIR)) ||
              (modifiableSnapshot.getStatics().getStateCounts().getOrDefault(secondState, 0) > getAvailableSecondaryBlockState() && !secondState.equals(BlockInformation.AIR))
              ||
              (modifiableSnapshot.getStatics().getStateCounts().getOrDefault(thirdState, 0) > getAvailableTertiaryBlockState() && !thirdState.equals(BlockInformation.AIR)))
        {
            return ItemStack.EMPTY;
        }

        if (consumeResources)
        {
            drainPrimaryStorage(modifiableSnapshot.getStatics().getStateCounts().getOrDefault(firstState, 0));
            drainSecondaryStorage(modifiableSnapshot.getStatics().getStateCounts().getOrDefault(secondState, 0));
            drainTertiaryStorage(modifiableSnapshot.getStatics().getStateCounts().getOrDefault(thirdState, 0));
        }

        return modifiableSnapshot.toItemStack().toBlockStack();
    }

    private void damageChisel()
    {
        if (getLevel() != null && !getLevel().isClientSide())
        {
            getToolStack().hurt(1, getLevel().getRandom(), null);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, @NotNull final Inventory playerInventory, @NotNull final Player playerEntity)
    {
        return new ChiseledPrinterContainer(
          containerId,
          playerInventory,
          getPatternHandler(),
          getToolHandler(),
          getResultHandler(),
          stationData);
    }

    public SimpleContainer getPatternHandler()
    {
        return pattern_handler.orElseThrow(() -> new IllegalStateException("Missing empty handler."));
    }

    public SimpleContainer getToolHandler()
    {
        return tool_handler.orElseThrow(() -> new IllegalStateException("Missing tool handler."));
    }

    public SimpleContainer getResultHandler()
    {
        return result_handler.orElseThrow(() -> new IllegalStateException("Missing result handler."));
    }

    @Override
    public @NotNull Component getDisplayName()
    {
        return LocalStrings.ChiselStationName.getText();
    }

    public int getAvailablePrimaryBlockState()
    {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise();

        return getStorageContents(targetedFacing);
    }

    public int getAvailableSecondaryBlockState()
    {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise().getClockWise();

        return getStorageContents(targetedFacing);
    }

    public int getAvailableTertiaryBlockState()
    {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.getCounterClockWise();

        return getStorageContents(targetedFacing);
    }

    private int getStorageContents(final Direction targetedFacing)
    {
        final BlockEntity targetedTileEntity = Objects.requireNonNull(this.getLevel()).getBlockEntity(this.getBlockPos().relative(targetedFacing));
        if (targetedTileEntity instanceof final BitStorageBlockEntity storage)
        {
            return storage.getBits();
        }

        return 0;
    }

    public BlockInformation getPrimaryBlockState()
    {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise();

        return getStorage(targetedFacing);
    }

    public BlockInformation getSecondaryBlockState()
    {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise().getClockWise();

        return getStorage(targetedFacing);
    }

    public BlockInformation getTertiaryBlockState()
    {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.getCounterClockWise();

        return getStorage(targetedFacing);
    }

    private BlockInformation getStorage(final Direction targetedFacing)
    {
        final BlockEntity targetedTileEntity = Objects.requireNonNull(this.getLevel()).getBlockEntity(this.getBlockPos().relative(targetedFacing));
        if (targetedTileEntity instanceof final BitStorageBlockEntity storage)
        {
            return storage.getContainedBlockInformation();
        }

        return BlockInformation.AIR;
    }

    public void drainPrimaryStorage(final int amount)
    {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise();

        drainStorage(amount, targetedFacing);
    }

    public void drainSecondaryStorage(final int amount)
    {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise().getClockWise();

        drainStorage(amount, targetedFacing);
    }

    public void drainTertiaryStorage(final int amount)
    {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.getCounterClockWise();

        drainStorage(amount, targetedFacing);
    }

    private void drainStorage(final int amount, final Direction targetedFacing)
    {
        final BlockEntity targetedTileEntity = Objects.requireNonNull(this.getLevel()).getBlockEntity(this.getBlockPos().relative(targetedFacing));
        if (targetedTileEntity instanceof final BitStorageBlockEntity storage)
        {
            storage.extractBits(amount);
        }
    }

    public void dropInventoryItems(Level worldIn, BlockPos pos)
    {
        Containers.dropItemStack(worldIn,
          pos.getX(),
          pos.getY(),
          pos.getZ(),
          getToolStack());

        Containers.dropItemStack(worldIn,
          pos.getX(),
          pos.getY(),
          pos.getZ(),
          getOutputStack());

        Containers.dropItemStack(worldIn,
          pos.getX(),
          pos.getY(),
          pos.getZ(),
          getPatternStack());
    }

    public ItemStack getToolStack()
    {
        return tool_handler.map(h -> h.getItem(0)).orElse(ItemStack.EMPTY);
    }

    public ItemStack getOutputStack()
    {
        return result_handler.map(h -> h.getItem(0)).orElse(ItemStack.EMPTY);
    }

    public ItemStack getPatternStack()
    {
        return pattern_handler.map(h -> h.getItem(0)).orElse(ItemStack.EMPTY);
    }

    @Override
    public int @NotNull [] getSlotsForFace(final @NotNull Direction direction)
    {
        return switch (direction)
                 {
                     case DOWN -> new int[] {2};
                     case UP -> new int[] {1};
                     case NORTH, SOUTH, WEST, EAST -> new int[] {0};
                 };
    }

    @Override
    public boolean canPlaceItemThroughFace(final int index, final ItemStack itemStack, final Direction direction)
    {
        return switch (direction)
                 {
                     case DOWN -> false;
                     case UP -> itemStack.getItem() instanceof IChiselItem;
                     case NORTH, SOUTH, WEST, EAST -> itemStack.getItem() instanceof IMultiUsePatternItem;
                 };
    }

    @Override
    public boolean canTakeItemThroughFace(final int index, final ItemStack itemStack, final @NotNull Direction direction)
    {
        return switch (direction)
                 {
                     case DOWN -> true;
                     case UP -> itemStack.getItem() instanceof IChiselItem;
                     case NORTH, SOUTH, WEST, EAST -> itemStack.getItem() instanceof IMultiUsePatternItem;
                 };
    }

    @Override
    public int getContainerSize()
    {
        return 3;
    }

    @Override
    public boolean isEmpty()
    {
        return getPatternHandler().isEmpty() &&
                 getToolHandler().isEmpty() &&
                 getResultHandler().isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(final int index)
    {
        return switch (index)
                 {
                     case 0 -> getPatternStack();
                     case 1 -> getToolStack();
                     case 2 -> getOutputStack();
                     default -> ItemStack.EMPTY;
                 };
    }

    @Override
    public @NotNull ItemStack removeItem(final int index, final int count)
    {
        return switch (index)
                 {
                     case 0 -> getPatternHandler().removeItem(0, count);
                     case 1 -> getToolHandler().removeItem(0, count);
                     case 2 -> getResultHandler().removeItem(0, count);
                     default -> ItemStack.EMPTY;
                 };
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(final int index)
    {
        return switch (index)
                 {
                     case 0 -> getPatternHandler().removeItemNoUpdate(0);
                     case 1 -> getToolHandler().removeItemNoUpdate(0);
                     case 2 -> getResultHandler().removeItemNoUpdate(0);
                     default -> ItemStack.EMPTY;
                 };
    }

    @Override
    public void setItem(final int index, final @NotNull ItemStack itemStack)
    {
        switch (index)
        {
            case 0 -> getPatternHandler().setItem(0, itemStack);
            case 1 -> getToolHandler().setItem(0, itemStack);
            case 2 -> getResultHandler().setItem(0, itemStack);
        }
    }

    @Override
    public boolean stillValid(final @NotNull Player player)
    {
        if (this.level.getBlockEntity(this.worldPosition) != this)
        {
            return false;
        }
        else
        {
            return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public void clearContent()
    {
        getPatternHandler().clearContent();
        getToolHandler().clearContent();
        getResultHandler().clearContent();
    }
}