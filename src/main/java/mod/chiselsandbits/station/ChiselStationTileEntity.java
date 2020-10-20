package mod.chiselsandbits.station;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.registry.ModTileEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChiselStationTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
    private final LazyOptional<EmptyHandler> empty_handler = LazyOptional.of(NonNullLazy.of(EmptyHandler::new));
    private final LazyOptional<ItemStackHandler> input_handler = LazyOptional.of(NonNullLazy.of(() -> new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
        {
            return BlockBitInfo.canChisel(stack);
        }
    }));
    private final LazyOptional<ItemStackHandler> working_handler = LazyOptional.of(NonNullLazy.of(() -> new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
        {
            return BlockBitInfo.canChisel(stack);
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
    private final LazyOptional<ItemStackHandler> tool_handler   = LazyOptional.of(NonNullLazy.of(() -> new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
        {
            return stack.getItem() instanceof ItemChisel;
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
            return stack.getItem() instanceof IPatternItem;
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
                return ChiselStationTileEntity.this.progress;
            }
            return 0;
        }

        public void set(int index, int value) {
            if (index == 0)
            {
                ChiselStationTileEntity.this.progress = value;
            }
        }

        public int size() {
            return 1;
        }
    };

    private int progress = 0;
    private long lastTickTime = 0L;
    private final MutableObject<ItemStack> currentRealisedWorkingStack = new MutableObject<>(ItemStack.EMPTY);

    public ChiselStationTileEntity()
    {
        super(ModTileEntityTypes.CHISEL_STATION.get());
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull final Capability<T> cap, @Nullable final Direction side)
    {
        if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return super.getCapability(cap, side);

        if (side == null) {
            return working_handler.cast();
        }

        switch(side){
            case DOWN:
                return result_handler.cast();
            case UP:
                return input_handler.cast();
            case NORTH:
            case SOUTH:
            case WEST:
            case EAST:
                return tool_handler.cast();
        }

        return empty_handler.cast();
    }

    @Override
    public void read(final BlockState state, final CompoundNBT nbt)
    {
        super.read(state, nbt);

        input_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("input")));
        working_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("working")));
        tool_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("tool")));
        pattern_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("pattern")));
        result_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("result")));

        progress = nbt.getInt("progress");
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);

        input_handler.ifPresent(h -> compound.put("input", h.serializeNBT()));
        working_handler.ifPresent(h -> compound.put("working", h.serializeNBT()));
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
            if (!hasWorkingStack() && hasInputStack() && hasMergeableInput()) {
                final ItemStack newWorkingStack = input_handler.map(h -> h.extractItem(0, 1, false)).orElse(ItemStack.EMPTY);
                if (!newWorkingStack.isEmpty()) {
                    working_handler.ifPresent(h -> h.insertItem(0, newWorkingStack, false));
                }
            }

            if (canWork()) {
                progress++;
                if (progress == 100) {
                    result_handler.ifPresent(h -> h.insertItem(0, getRealisedStack(), false));
                    working_handler.ifPresent(h -> h.extractItem(0, Integer.MAX_VALUE, false));

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

    public IItemHandlerModifiable getWorkingHandler() {
        return working_handler.orElseThrow(() -> new IllegalStateException("Missing working handler."));
    }

    public IItemHandlerModifiable getInputHandler() {
        return input_handler.orElseThrow(() -> new IllegalStateException("Missing input handler."));
    }

    public IItemHandlerModifiable getResultHandler() {
        return result_handler.orElseThrow(() -> new IllegalStateException("Missing result handler."));
    }

    public boolean hasInputStack() {
        return !getInputStack().isEmpty();
    }

    public boolean hasWorkingStack() {
        return !getWorkingStack().isEmpty();
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
        return hasWorkingStack() && hasPatternStack() && hasToolStack() && canMergeOutputs();
    }

    public boolean couldWork() {
        return hasPatternStack() && hasToolStack();
    }

    public boolean hasMergeableInput() {
        if (!hasOutputStack())
            return true;

        return ItemHandlerHelper.canItemStacksStack(getOutputStack(), realisePatternFromInput());
    }

    public ItemStack getInputStack() {
        return input_handler.map(h -> h.getStackInSlot(0)).orElse(ItemStack.EMPTY);
    }

    public ItemStack getWorkingStack() {
        return working_handler.map(h -> h.getStackInSlot(0)).orElse(ItemStack.EMPTY);
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
            realisedStack = realisePattern();
            currentRealisedWorkingStack.setValue(realisedStack);
        }

        return realisedStack;
    }

    public ItemStack getOutputStack() {
        return result_handler.map(h -> h.getStackInSlot(0)).orElse(ItemStack.EMPTY);
    }

    public ItemStack realisePatternFromInput() {
        if (!hasInputStack())
            return ItemStack.EMPTY;

        final ItemStack inputStack = getInputStack();
        return realisePattern(inputStack);
    }

    private ItemStack realisePattern() {
        if (!hasWorkingStack())
            return ItemStack.EMPTY;

        final ItemStack workingStack = getWorkingStack();
        return realisePattern(workingStack);
    }

    private ItemStack realisePattern(final ItemStack source) {
        if (!hasPatternStack())
            return ItemStack.EMPTY;

        final ItemStack stack = getPatternStack();
        if (!(stack.getItem() instanceof IPatternItem))
            return ItemStack.EMPTY;

        final IPatternItem patternItem = (IPatternItem) stack.getItem();
        final ItemStack realisedPattern = patternItem.getPatternedItem(stack.copy(), true);
        if (realisedPattern == null || realisedPattern.isEmpty())
            return ItemStack.EMPTY;

        final ItemStack workingStack = source.copy();
        final Item workingItem = workingStack.getItem();
        if (!(workingItem instanceof BlockItem))
            return ItemStack.EMPTY;

        final BlockItem blockItem = (BlockItem) workingItem;
        final Block block = blockItem.getBlock();
        final BlockState blockState = block.getDefaultState();
        if (!BlockBitInfo.isSupported(blockState))
            return ItemStack.EMPTY;

        final NBTBlobConverter c = new NBTBlobConverter();
        final CompoundNBT tag = ModUtil.getSubCompound(realisedPattern, ModUtil.NBT_BLOCKENTITYTAG, false).copy();
        c.readChisleData(tag, VoxelBlob.VERSION_ANY);
        VoxelBlob blob = c.getBlob();

        blob.fillNoneAir(ModUtil.getStateId(blockState));
        c.setBlob(blob);

        final BlockState state = c.getPrimaryBlockState();
        final ItemStack itemstack = new ItemStack(ModBlocks.convertGivenStateToChiseledBlock(state), 1 );
        c.writeChisleData(tag, false);

        itemstack.setTagInfo( ModUtil.NBT_BLOCKENTITYTAG, tag );
        return itemstack;
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
        return new ChiselStationContainer(
          containerId,
          playerInventory,
          getPatternHandler(),
          getInputHandler(),
          getWorkingHandler(),
          getToolHandler(),
          getResultHandler(),
          stationData);
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return LocalStrings.ChiselStationName.getLocalText();
    }
}
