package mod.chiselsandbits.block.entities;

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
import mod.chiselsandbits.registrars.ModTileEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChiseledPrinterBlockEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
    private final LazyOptional<EmptyHandler>     empty_handler = LazyOptional.of(NonNullLazy.of(EmptyHandler::new));
    private final LazyOptional<ItemStackHandler> tool_handler  = LazyOptional.of(NonNullLazy.of(() -> new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
        {
            return stack.getItem() instanceof IChiselItem;
        }

        @Override
        public int getSlotLimit(final int slot)
        {
            return 1;
        }
    }));
    private final LazyOptional<ItemStackHandler> pattern_handler   = LazyOptional.of(NonNullLazy.of(() -> new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
        {
            return stack.getItem() instanceof IMultiUsePatternItem;
        }

        @Override
        public int getSlotLimit(final int slot)
        {
            return 1;
        }

        @Override
        protected void onContentsChanged(final int slot)
        {
            currentRealisedWorkingStack.setValue(ItemStack.EMPTY);
        }
    }));
    private final LazyOptional<ItemStackHandler> result_handler = LazyOptional.of(NonNullLazy.of(() -> new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
        {
            return true;
        }
    }));

    protected final IIntArray stationData = new IIntArray() {
        public int get(int index) {
            if (index == 0)
            {
                return ChiseledPrinterBlockEntity.this.progress;
            }
            return 0;
        }

        public void set(int index, int value) {
            if (index == 0)
            {
                ChiseledPrinterBlockEntity.this.progress = value;
            }
        }

        public int size() {
            return 1;
        }
    };

    private int progress = 0;
    private       long                     lastTickTime                = 0L;
    private final MutableObject<ItemStack> currentRealisedWorkingStack = new MutableObject<>(ItemStack.EMPTY);

    public ChiseledPrinterBlockEntity()
    {
        super(ModTileEntityTypes.CHISELED_PRINTER.get());
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull final Capability<T> cap, @Nullable final Direction side)
    {
        if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return super.getCapability(cap, side);

        if (side != null)
        {
            switch(side){
                case DOWN:
                    return result_handler.cast();
                case UP:
                case NORTH:
                case SOUTH:
                case WEST:
                case EAST:
                    return tool_handler.cast();
            }
        }

        return empty_handler.cast();
    }

    @Override
    public void read(final BlockState state, final CompoundNBT nbt)
    {
        super.read(state, nbt);

        tool_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("tool")));
        pattern_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("pattern")));
        result_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("result")));

        progress = nbt.getInt("progress");
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);

        tool_handler.ifPresent(h -> compound.put("tool", h.serializeNBT()));
        pattern_handler.ifPresent(h -> compound.put("pattern", h.serializeNBT()));
        result_handler.ifPresent(h -> compound.put("result", h.serializeNBT()));

        compound.putInt("progress", progress);

        return compound;
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        final CompoundNBT nbt = new CompoundNBT();
        write(nbt);
        return nbt;
    }

    @Override
    public void tick()
    {
        if (getWorld() == null || lastTickTime == getWorld().getGameTime() || getWorld().isRemote()) {
            return;
        }

        this.lastTickTime = getWorld().getGameTime();

        if (couldWork()) {
            if (canWork()) {
                progress++;
                if (progress >= 100) {
                    result_handler.ifPresent(h -> h.insertItem(0, realisePattern(true), false));
                    currentRealisedWorkingStack.setValue(ItemStack.EMPTY);
                    progress = 0;
                    damageChisel();
                }
                markDirty();
            }
        } else if (progress != 0) {
            progress = 0;
            markDirty();
        }
    }

    public IItemHandlerModifiable getPatternHandler() {
        return pattern_handler.orElseThrow(() -> new IllegalStateException("Missing empty handler."));
    }

    public IItemHandlerModifiable getToolHandler() {
        return tool_handler.orElseThrow(() -> new IllegalStateException("Missing tool handler."));
    }

    public IItemHandlerModifiable getResultHandler() {
        return result_handler.orElseThrow(() -> new IllegalStateException("Missing result handler."));
    }

    public boolean hasPatternStack() {
        return !getPatternStack().isEmpty();
    }

    public boolean hasToolStack() {
        return !getToolStack().isEmpty();
    }

    public boolean hasRealisedStack() {
        return !getRealisedStack().isEmpty();
    }

    public boolean hasOutputStack() {
        return !getOutputStack().isEmpty();
    }

    public boolean canMergeOutputs() {
        if (!hasOutputStack())
            return true;

        if (!hasRealisedStack())
            return false;

        return ItemHandlerHelper.canItemStacksStack(getOutputStack(), getRealisedStack());
    }

    public boolean canWork() {
        return hasPatternStack() && hasToolStack() && canMergeOutputs() && !getRealisedStack().isEmpty();
    }

    public boolean couldWork() {
        return hasPatternStack() && hasToolStack();
    }

    public ItemStack getPatternStack() {
        return pattern_handler.map(h -> h.getStackInSlot(0)).orElse(ItemStack.EMPTY);
    }

    public ItemStack getToolStack() {
        return tool_handler.map(h -> h.getStackInSlot(0)).orElse(ItemStack.EMPTY);
    }

    public ItemStack getRealisedStack() {
        ItemStack realisedStack = currentRealisedWorkingStack.getValue();
        if (realisedStack.isEmpty()) {
            realisedStack = realisePattern(false);
            currentRealisedWorkingStack.setValue(realisedStack);
        }

        return realisedStack;
    }

    public ItemStack getOutputStack() {
        return result_handler.map(h -> h.getStackInSlot(0)).orElse(ItemStack.EMPTY);
    }

    private ItemStack realisePattern(final boolean consumeResources) {
        if (!hasPatternStack())
            return ItemStack.EMPTY;

        final ItemStack stack = getPatternStack();
        if (!(stack.getItem() instanceof IPatternItem))
            return ItemStack.EMPTY;

        final IPatternItem patternItem = (IPatternItem) stack.getItem();
        final IMultiStateItemStack realisedPattern = patternItem.createItemStack(stack);
        if (realisedPattern.getStatistics().isEmpty())
            return ItemStack.EMPTY;

        final BlockState firstState = getPrimaryBlockState() == null ? Blocks.AIR.getDefaultState() : getPrimaryBlockState();
        final BlockState secondState = getSecondaryBlockState() == null ? Blocks.AIR.getDefaultState() : getSecondaryBlockState();
        final BlockState thirdState = getTertiaryBlockState() == null ? Blocks.AIR.getDefaultState() : getTertiaryBlockState();

        if (firstState.isAir() && secondState.isAir() && thirdState.isAir())
            return ItemStack.EMPTY;

        if ((!IEligibilityManager.getInstance().canBeChiseled(firstState) && !firstState.isAir())
              || (!IEligibilityManager.getInstance().canBeChiseled(secondState) && !secondState.isAir())
              || (!IEligibilityManager.getInstance().canBeChiseled(thirdState) && !thirdState.isAir())
        )
            return ItemStack.EMPTY;

        final IMultiStateSnapshot modifiableSnapshot = realisedPattern.createSnapshot();
        modifiableSnapshot.mutableStream()
          .filter(e -> (e.getState() != firstState || firstState == Blocks.AIR.getDefaultState()) && (e.getState() != secondState || secondState == Blocks.AIR.getDefaultState()) && (e.getState() != thirdState || thirdState == Blocks.AIR.getDefaultState()) && e.getState() != Blocks.AIR.getDefaultState())
          .forEach(IMutableStateEntryInfo::clear);

        if (modifiableSnapshot.getStatics().getStateCounts().getOrDefault(firstState, 0) == 0 &&
              modifiableSnapshot.getStatics().getStateCounts().getOrDefault(secondState, 0) == 0 &&
              modifiableSnapshot.getStatics().getStateCounts().getOrDefault(thirdState, 0) == 0
        )
            return ItemStack.EMPTY;

        if ((modifiableSnapshot.getStatics().getStateCounts().getOrDefault(firstState, 0) > getAvailablePrimaryBlockState() && firstState != Blocks.AIR.getDefaultState()) ||
              (modifiableSnapshot.getStatics().getStateCounts().getOrDefault(secondState, 0) > getAvailableSecondaryBlockState() && secondState != Blocks.AIR.getDefaultState())  ||
              (modifiableSnapshot.getStatics().getStateCounts().getOrDefault(thirdState, 0) > getAvailableTertiaryBlockState() && thirdState != Blocks.AIR.getDefaultState()) )
            return ItemStack.EMPTY;

        if (consumeResources) {
            drainPrimaryStorage(modifiableSnapshot.getStatics().getStateCounts().getOrDefault(firstState, 0));
            drainSecondaryStorage(modifiableSnapshot.getStatics().getStateCounts().getOrDefault(secondState, 0));
            drainTertiaryStorage(modifiableSnapshot.getStatics().getStateCounts().getOrDefault(thirdState, 0));
        }

        return modifiableSnapshot.toItemStack().toBlockStack();
    }

    private void damageChisel() {
        if (getWorld() != null && !getWorld().isRemote())
        {
            getToolStack().attemptDamageItem(1, getWorld().getRandom(), null);
        }
    }

    @Nullable
    @Override
    public Container createMenu(final int containerId, @NotNull final PlayerInventory playerInventory, @NotNull final PlayerEntity playerEntity)
    {
        return new ChiseledPrinterContainer(
          containerId,
          playerInventory,
          getPatternHandler(),
          getToolHandler(),
          getResultHandler(),
          stationData);
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return LocalStrings.ChiselStationName.getLocalText();
    }

    public int getAvailablePrimaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()).get(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.rotateY();

        return getStorageContents(targetedFacing);
    }

    public int getAvailableSecondaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()).get(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.rotateY().rotateY();

        return getStorageContents(targetedFacing);
    }

    public int getAvailableTertiaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()).get(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.rotateYCCW();

        return getStorageContents(targetedFacing);
    }

    private int getStorageContents(final Direction targetedFacing)
    {
        final TileEntity targetedTileEntity = Objects.requireNonNull(this.getWorld()).getTileEntity(this.getPos().offset(targetedFacing));
        if (targetedTileEntity instanceof BitStorageBlockEntity)
        {
            final BitStorageBlockEntity storage = (BitStorageBlockEntity) targetedTileEntity;
            return storage.getBits();
        }

        return 0;
    }

    public BlockState getPrimaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()).get(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.rotateY();

        return getStorage(targetedFacing);
    }

    public BlockState getSecondaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()).get(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.rotateY().rotateY();

        return getStorage(targetedFacing);
    }

    public BlockState getTertiaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()).get(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.rotateYCCW();

        return getStorage(targetedFacing);
    }

    private BlockState getStorage(final Direction targetedFacing)
    {
        final TileEntity targetedTileEntity = Objects.requireNonNull(this.getWorld()).getTileEntity(this.getPos().offset(targetedFacing));
        if (targetedTileEntity instanceof BitStorageBlockEntity)
        {
            final BitStorageBlockEntity storage = (BitStorageBlockEntity) targetedTileEntity;
            return storage.getState();
        }

        return Blocks.AIR.getDefaultState();
    }

    public void drainPrimaryStorage(final int amount) {
        final Direction facing = Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()).get(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.rotateY();

        drainStorage(amount, targetedFacing);
    }

    public void drainSecondaryStorage(final int amount) {
        final Direction facing = Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()).get(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.rotateY().rotateY();

        drainStorage(amount, targetedFacing);
    }

    public void drainTertiaryStorage(final int amount) {
        final Direction facing = Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()).get(ChiseledPrinterBlock.FACING);
        final Direction targetedFacing = facing.rotateYCCW();

        drainStorage(amount, targetedFacing);
    }

    private void drainStorage(final int amount, final Direction targetedFacing)
    {
        final TileEntity targetedTileEntity = Objects.requireNonNull(this.getWorld()).getTileEntity(this.getPos().offset(targetedFacing));
        if (targetedTileEntity instanceof BitStorageBlockEntity)
        {
            final BitStorageBlockEntity storage = (BitStorageBlockEntity) targetedTileEntity;
            storage.extractBits(0, amount, false);
        }
    }

    public void dropInventoryItems(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        InventoryHelper.spawnItemStack(worldIn,
          pos.getX(),
          pos.getY(),
          pos.getZ(),
          getToolStack());

        InventoryHelper.spawnItemStack(worldIn,
          pos.getX(),
          pos.getY(),
          pos.getZ(),
          getOutputStack());

        InventoryHelper.spawnItemStack(worldIn,
          pos.getX(),
          pos.getY(),
          pos.getZ(),
          getPatternStack());
    }
}