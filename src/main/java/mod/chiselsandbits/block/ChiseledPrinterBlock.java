package mod.chiselsandbits.block;
import com.google.common.collect.ImmutableMap;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.block.entities.ChiseledPrinterBlockEntity;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.block.AbstractBlock.Properties;

public class ChiseledPrinterBlock extends ContainerBlock
{

    public static final  DirectionProperty FACING   = HorizontalBlock.FACING;

    private static final Map<Direction, VoxelShape> BUTTON_VS_MAP = ImmutableMap.<Direction, VoxelShape>builder()
                                                                      .put(Direction.NORTH, Block.box(7, 1, -0.5, 12, 4, 0))
                                                                      .put(Direction.EAST, Block.box(16,1,7,16.5,4,12))
                                                                      .put(Direction.SOUTH, Block.box(4, 1, 16, 9, 4, 16.5))
                                                                      .put(Direction.WEST, Block.box(-0.5, 1, 4, 0, 4, 9))
                                                                      .build();

    private static final VoxelShape        VS_NORTH = createForDirection(Direction.NORTH);
    private static final VoxelShape        VS_EAST = createForDirection(Direction.EAST);
    private static final VoxelShape        VS_SOUTH = createForDirection(Direction.SOUTH);
    private static final VoxelShape        VS_WEST = createForDirection(Direction.WEST);

    private static final Map<Direction, VoxelShape> VS_MAP = ImmutableMap.<Direction, VoxelShape>builder()
                                                               .put(Direction.NORTH, VS_NORTH)
                                                               .put(Direction.EAST, VS_EAST)
                                                               .put(Direction.SOUTH, VS_SOUTH)
                                                               .put(Direction.WEST, VS_WEST)
                                                               .build();

    private static VoxelShape createForDirection(final Direction direction)
    {
        return VoxelShapes.join(VoxelShapes.join(Stream.of(
          Block.box(0, 5, 0, 2, 16, 2),
          Block.box(14, 5, 0, 16, 16, 2),
          Block.box(0, 5, 14, 2, 16, 16),
          Block.box(14, 5, 14, 16, 16, 16)
          ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).orElse(VoxelShapes.empty()), Stream.of(
          Block.box(2, 14, 0, 14, 16, 2),
          Block.box(2, 14, 14, 14, 16, 16),
          Block.box(0, 14, 2, 2, 16, 14),
          Block.box(14, 14, 2, 16, 16, 14),
          Stream.of(
            Block.box(2, 14, 7, 14, 16, 9),
            Block.box(7, 13.99, 2, 9, 15.98, 14),
            Block.box(7, 11, 7, 9, 14, 9),
            Block.box(7.5, 10, 7.5, 8.5, 11, 8.5)
          ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).orElse(VoxelShapes.empty())
          ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).orElse(VoxelShapes.empty()), IBooleanFunction.OR),
          VoxelShapes.join(Block.box(0, 0, 0, 16, 5, 16), BUTTON_VS_MAP.getOrDefault(direction, VoxelShapes.empty()), IBooleanFunction.OR),
          IBooleanFunction.OR);
    }

    public ChiseledPrinterBlock(final Properties builder)
    {
        super(builder);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public @NotNull BlockRenderType getRenderShape(@NotNull BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    public @NotNull BlockState rotate(BlockState state, Rotation rot)
    {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public @NotNull BlockState mirror(BlockState state, Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public @NotNull VoxelShape getShape(final BlockState state, final @NotNull IBlockReader worldIn, final @NotNull BlockPos pos, final @NotNull ISelectionContext context)
    {
        return VS_MAP.getOrDefault(state.getValue(FACING), VoxelShapes.empty());
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(final @NotNull IBlockReader worldIn)
    {
        return new ChiseledPrinterBlockEntity();
    }

    public @NotNull ActionResultType use(@NotNull BlockState state, World worldIn, @NotNull BlockPos pos, @NotNull PlayerEntity player, @NotNull Hand handIn, @NotNull BlockRayTraceResult hit)
    {
        if (worldIn.isClientSide)
        {
            return ActionResultType.SUCCESS;
        }
        else
        {
            player.openMenu((INamedContainerProvider) worldIn.getBlockEntity(pos));
            return ActionResultType.CONSUME;
        }
    }

    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final IBlockReader worldIn, final @NotNull List<ITextComponent> tooltip, final @NotNull ITooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        ChiselsAndBits.getInstance().getConfiguration().getCommon().helpText(LocalStrings.ChiselStationHelp, tooltip);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, @NotNull World worldIn, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof ChiseledPrinterBlockEntity) {
                ((ChiseledPrinterBlockEntity) tileentity).dropInventoryItems(worldIn, pos);
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }
}