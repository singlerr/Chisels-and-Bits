package mod.chiselsandbits.block;

import com.google.common.collect.Lists;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class BitStorageBlock extends Block implements EntityBlock
{

    public static final Property<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public BitStorageBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final @NotNull BlockPos pos, final @NotNull BlockState state)
    {
        return new BitStorageBlockEntity(pos, state);
    }

    @Override
    public @NotNull InteractionResult use(
      final @NotNull BlockState state, final Level worldIn, final @NotNull BlockPos pos, final @NotNull Player player, final @NotNull InteractionHand handIn, final @NotNull BlockHitResult hit)
    {
        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (!(tileEntity instanceof final BitStorageBlockEntity tank))
            return InteractionResult.FAIL;

        final ItemStack current = player.getInventory().getSelected();

        if (!current.isEmpty())
        {
            if (FluidUtil.interactWithFluidHandler(player, handIn, tank))
            {
                return InteractionResult.SUCCESS;
            }

            if (tank.addHeldBits(current, player))
            {
                return InteractionResult.SUCCESS;
            }
        }
        else
        {
            if (tank.addAllPossibleBits(player))
            {
                return InteractionResult.SUCCESS;
            }
        }

        if (tank.extractBits(player, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, pos))
        {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos)
    {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos)
    {
        return true;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(final @NotNull BlockState state, final LootContext.Builder builder)
    {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) == null)
        {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(getTankDrop((BitStorageBlockEntity) builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)));
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
}