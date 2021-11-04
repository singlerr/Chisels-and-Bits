package mod.chiselsandbits.block.entities;

import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.util.SingleBlockWorldReader;
import mod.chiselsandbits.block.BitStorageBlock;
import mod.chiselsandbits.platforms.core.blockstate.ILevelBasedPropertyAccessor;
import mod.chiselsandbits.platforms.core.fluid.FluidInformation;
import mod.chiselsandbits.platforms.core.fluid.IFluidManager;
import mod.chiselsandbits.platforms.core.registrars.IBlockEntityRegistrar;
import mod.chiselsandbits.utils.BitInventoryUtils;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BitStorageBlockEntity extends BlockEntity implements Container
{
    private BlockState state = null;
    private int        bits  = 0;

    private int oldLV = -1;

    public BitStorageBlockEntity(BlockPos pos, BlockState state)
    {
        super(IBlockEntityRegistrar.getInstance().getBitStorage().get(), pos, state);
    }

    @Override
    public void load(final @NotNull CompoundTag nbt)
    {
        super.load(nbt);
        if (nbt.contains("state"))
        {
            final CompoundTag stateCompound = nbt.getCompound("state");
            this.state = NbtUtils.readBlockState(stateCompound);
        }
        else
        {
            final int rawState = nbt.getInt("blockstate");
            if (rawState != -1)
            {
                this.state = IBlockStateIdManager.getInstance().getBlockStateFrom(rawState);
            }
            else
            {
                this.state = null;
            }
        }

        if (state != null)
        {
            bits = nbt.getInt("bits");
        }
        else
        {
            bits = 0;
        }
    }

    @Override
    public @NotNull CompoundTag save(final @NotNull CompoundTag compound)
    {
        final CompoundTag nbt = super.save(compound);
        nbt.put("state", state == null ? new CompoundTag() : NbtUtils.writeBlockState(state));
        nbt.putInt("bits", bits);
        return nbt;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        final CompoundTag t = new CompoundTag();
        return new ClientboundBlockEntityDataPacket(getBlockPos(), 0, save(t));
    }

    @Override
    public @NotNull CompoundTag getUpdateTag()
    {
        final CompoundTag nbttagcompound = new CompoundTag();
        return save(nbttagcompound);
    }

    private void saveAndUpdate()
    {
        if (level == null || getLevel() == null)
        {
            return;
        }

        if (bits == 0)
        {
            this.state = null;
        }

        if (state == null)
        {
            this.bits = 0;
        }

        setChanged();
        getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 0);

        final int lv = getLightValue();
        if (oldLV != lv)
        {
            getLevel().getLightEngine().checkBlock(getBlockPos());
            oldLV = lv;
        }
    }

    public int getLightValue()
    {
        return ILevelBasedPropertyAccessor.getInstance().getLightEmission(
          new SingleBlockWorldReader(
            state,
            getBlockPos(),
            getLevel()
          ),
          getBlockPos()
        );
    }

    public boolean addAllPossibleBits(
      final Player playerIn)
    {
        if (playerIn != null && playerIn.isShiftKeyDown() && state != null && !state.isAir())
        {
            final IBitInventory bitInventory = IBitInventoryManager.getInstance().create(playerIn);
            final int extractionAmount = Math.min(
              StateEntrySize.current().getBitsPerBlock() - bits,
              bitInventory.getMaxExtractAmount(state)
            );

            bitInventory.extract(state, extractionAmount);

            bits += extractionAmount;
            saveAndUpdate();
        }

        return false;
    }

    public boolean addHeldBits(
      final ItemStack current,
      final Player playerIn)
    {
        if (playerIn.isShiftKeyDown() || this.bits == 0)
        {
            if (current.getItem() instanceof IBitItem bitItem)
            {
                if (bitItem.getBitState(current) == state || state == null) {
                    final int maxToInsert = StateEntrySize.current().getBitsPerBlock() - bits;
                    final int toInsert = Math.min(maxToInsert, current.getCount());

                    bits += toInsert;

                    if (!playerIn.isCreative())
                    {
                        current.shrink(toInsert);
                        playerIn.getInventory().setItem(playerIn.getInventory().selected, current);
                        playerIn.getInventory().setChanged();
                    }
                    saveAndUpdate();
                    return true;
                }
            }
            else if (IEligibilityManager.getInstance().canBeChiseled(current.getItem()))
            {
                final BlockState stackState = ItemStackUtils.getStateFromItem(current);
                if (stackState.getBlock() != Blocks.AIR)
                {
                    if (this.state == null || state.isAir())
                    {
                        this.state = stackState;
                        this.bits = StateEntrySize.current().getBitsPerBlock();

                        if (!playerIn.isCreative())
                        {
                            current.shrink(1);
                            playerIn.getInventory().setItem(playerIn.getInventory().selected, current);
                            playerIn.getInventory().setChanged();
                        }
                        saveAndUpdate();
                        return true;
                    }
                }
            }


            final Optional<FluidInformation> containedFluid = IFluidManager.getInstance().get(current);
            if (containedFluid.isPresent() && containedFluid.get().amount() > 0) {
                final BlockState state = containedFluid.get().fluid().defaultFluidState().createLegacyBlock();

                if (IEligibilityManager.getInstance().canBeChiseled(state)) {
                    final int maxToInsert = StateEntrySize.current().getBitsPerBlock() - bits;
                    final int toInsert = (int) Math.min(maxToInsert, getBitCountFrom(containedFluid.get()));

                    bits += toInsert;

                    if (!playerIn.isCreative())
                    {
                        final ItemStack resultStack = IFluidManager.getInstance().extractFrom(current, toInsert);
                        playerIn.getInventory().setItem(playerIn.getInventory().selected, resultStack);
                        playerIn.getInventory().setChanged();
                    }
                    saveAndUpdate();
                    return true;
                }
            }
        }

        return false;
    }

    private float getBitCountFrom(final FluidInformation containedFluid)
    {
        return StateEntrySize.current()
          .getBitsPerBlock() * (containedFluid.amount() / (float) IFluidManager.getInstance().getBucketAmount());
    }

    public boolean extractBits(
      final Player playerIn
    )
    {
        if (!playerIn.isShiftKeyDown())
        {
            final ItemStack is = getItem(0);
            if (!is.isEmpty())
            {
                if (is.getItem() instanceof final IBitItem bitItem)
                {
                    final BlockState blockState = bitItem.getBitState(is);

                    BitInventoryUtils.insertIntoOrSpawn(
                      playerIn,
                      blockState,
                      is.getCount()
                    );

                    removeItem(0, is.getCount());
                }
            }
            return true;
        }

        return false;
    }

    public BlockState getState()
    {
        return state;
    }

    public int getBits()
    {
        return bits;
    }

    public Direction getFacing()
    {
        return getLevel().getBlockState(getBlockPos()).getValue(BitStorageBlock.FACING);
    }

    @Override
    public int getContainerSize()
    {
        return 1;
    }

    @Override
    public boolean isEmpty()
    {
        return state == null || bits == 0;
    }

    @Override
    public @NotNull ItemStack getItem(final int index)
    {
        if (index != 0)
            return ItemStack.EMPTY;

        return IBitItemManager.getInstance().create(state, Math.min(64, bits));
    }

    @Override
    public @NotNull ItemStack removeItem(final int index, final int count)
    {
        if (index != 0)
            return ItemStack.EMPTY;

        final int toRemove = Math.min(count, bits);
        bits -= toRemove;

        saveAndUpdate();
        return IBitItemManager.getInstance().create(state, toRemove);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(final int index)
    {
        //Not supported
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(final int index, final ItemStack itemStack)
    {
        if (index != 0 || !(itemStack.getItem() instanceof IBitItem) || ((IBitItem) itemStack.getItem()).getBitState(itemStack) == state)
            return;

        saveAndUpdate();
        bits = Math.max(StateEntrySize.current().getBitsPerBlock(), bits + itemStack.getCount());
    }

    @Override
    public boolean stillValid(final Player player)
    {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(player.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) > 64.0D);
        }
    }

    @Override
    public void clearContent()
    {
        this.state = null;
        this.bits = 0;
        saveAndUpdate();
    }

    public boolean containsFluid() {
        return state != null && state.getBlock() instanceof LiquidBlock liquidBlock && !liquidBlock.getFluidState(state).isEmpty();
    }

    public Optional<FluidInformation> getFluid() {
        if (!containsFluid())
            return Optional.empty();

        return Optional.of(new FluidInformation(
          ((LiquidBlock) state.getBlock()).getFluidState(state).getType(),
          (long) (bits / (StateEntrySize.current().getBitsPerBlock() / (float) IFluidManager.getInstance().getBucketAmount())),
          new CompoundTag()
        ));
    }

    public void extractBits(final int count)
    {
        this.bits = Math.max(0, this.bits - count);
        if (this.bits <= 0)
        {
            this.state = null;
        }
        saveAndUpdate();
    }

    public void insertBits(final int bitCountToInsert, final BlockState containedState)
    {
        if (state == null || containedState == state) {
            this.bits = Math.max(StateEntrySize.current().getBitsPerBlock(), bitCountToInsert + bits);
            this.state = containedState;
            saveAndUpdate();
        }
    }

    public void insertBitsFromFluid(final FluidInformation fluidInformation)
    {
        if (state == null || state == fluidInformation.fluid().defaultFluidState().createLegacyBlock()) {
            this.bits = (int) Math.max(StateEntrySize.current().getBitsPerBlock(), getBitCountFrom(fluidInformation) + bits);
            this.state = fluidInformation.fluid().defaultFluidState().createLegacyBlock();
            saveAndUpdate();
        }
    }
}
