package mod.chiselsandbits.block;

import com.google.common.collect.Lists;
import mod.chiselsandbits.api.block.bitbag.IBitBagAcceptingBlock;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.item.BitBagItem;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.Option;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class BitStorageBlock extends Block implements ITileEntityProvider, IBitBagAcceptingBlock
{

    public static final Property<Direction> FACING = HorizontalBlock.FACING;

    public BitStorageBlock(AbstractBlock.Properties properties)
    {
        super(properties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(final @NotNull IBlockReader worldIn)
    {
        return new BitStorageBlockEntity();
    }

    @Override
    public @NotNull ActionResultType use(
      final @NotNull BlockState state, final World worldIn, final @NotNull BlockPos pos, final @NotNull PlayerEntity player, final @NotNull Hand handIn, final @NotNull BlockRayTraceResult hit)
    {
        final TileEntity tileEntity = worldIn.getBlockEntity(pos);
        if (!(tileEntity instanceof BitStorageBlockEntity))
            return ActionResultType.FAIL;

        final BitStorageBlockEntity tank = (BitStorageBlockEntity) tileEntity;
        final ItemStack current = player.inventory.getSelected();

        if (current.getItem() instanceof BitBagItem)
            return ActionResultType.PASS;

        if (!current.isEmpty())
        {
            if (FluidUtil.interactWithFluidHandler(player, handIn, tank))
            {
                return ActionResultType.SUCCESS;
            }

            if (tank.addHeldBits(current, player))
            {
                return ActionResultType.SUCCESS;
            }
        }
        else
        {
            if (tank.addAllPossibleBits(player))
            {
                return ActionResultType.SUCCESS;
            }
        }

        if (tank.extractBits(player, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, pos))
        {
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
    }

    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(@NotNull BlockState state, @NotNull IBlockReader worldIn, @NotNull BlockPos pos)
    {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull IBlockReader reader, @NotNull BlockPos pos)
    {
        return true;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(final @NotNull BlockState state, final LootContext.Builder builder)
    {
        if (builder.getOptionalParameter(LootParameters.BLOCK_ENTITY) == null)
        {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(getTankDrop((BitStorageBlockEntity) builder.getOptionalParameter(LootParameters.BLOCK_ENTITY)));
    }

    public ItemStack getTankDrop(final BitStorageBlockEntity bitTank)
    {
        final ItemStack tankStack = new ItemStack(ModItems.BIT_STORAGE.get());
        tankStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
          .ifPresent(s -> s
                            .fill(
                              bitTank
                                .getCapability(
                                  CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                                )
                                .map(
                                  t -> t
                                         .drain(
                                           Integer.MAX_VALUE,
                                           IFluidHandler.FluidAction.EXECUTE
                                         )
                                ).orElse(FluidStack.EMPTY),
                              IFluidHandler.FluidAction.EXECUTE
                            )
          );
        return tankStack;
    }

    @Override
    public void onBitBagInteraction(final ItemStack bitBagStack, final PlayerEntity player, final BlockRayTraceResult blockRayTraceResult)
    {
        final TileEntity tileEntity = player.level.getBlockEntity(blockRayTraceResult.getBlockPos());
        if (!(tileEntity instanceof BitStorageBlockEntity))
            return;

        final BitStorageBlockEntity tank = (BitStorageBlockEntity) tileEntity;
        final IBitInventory bitInventory = IBitInventoryManager.getInstance().create(bitBagStack);

        final BlockState containedState = tank.getState();
        final Fluid containedFluid = tank.getMyFluid();

        if (player.isCrouching() && (containedFluid != null || containedState != null)) {
            final BlockState toInsertState = containedFluid != null ? containedFluid.defaultFluidState().createLegacyBlock() : containedState;
            final int maxAmountToInsert = bitInventory.getMaxInsertAmount(toInsertState);
            final int bitCountToInsert = Math.min(tank.getBits(), maxAmountToInsert);

            tank.extractBits(0, bitCountToInsert, false);
            bitInventory.insert(toInsertState, bitCountToInsert);
        }
        else if ((containedFluid != null || containedState != null) && tank.getBits() != 0) {
            final BlockState toExtractState = containedFluid != null ? containedFluid.defaultFluidState().createLegacyBlock() : containedState;
            final int maxAmountToInsert = StateEntrySize.current().getBitsPerBlock() - tank.getBits();
            final int bitCountToInsert = Math.min(bitInventory.getMaxExtractAmount(toExtractState), maxAmountToInsert);

            tank.insertBits(bitCountToInsert, toExtractState, false);
            bitInventory.extract(toExtractState, bitCountToInsert);
        }
        else if (!player.isCrouching()) {
            final Optional<BlockState> toExtractCandidate =
                bitInventory.getContainedStates()
                  .entrySet()
                  .stream()
                  .max(Map.Entry.comparingByValue())
                  .map(Map.Entry::getKey);
            if (toExtractCandidate.isPresent()) {
                final BlockState toExtractState = toExtractCandidate.get();
                final int maxAmountToInsert = StateEntrySize.current().getBitsPerBlock();
                final int bitCountToInsert = Math.min(bitInventory.getMaxExtractAmount(toExtractState), maxAmountToInsert);

                tank.insertBits(bitCountToInsert, toExtractState, false);
                bitInventory.extract(toExtractState, bitCountToInsert);
            }
        }


    }
}