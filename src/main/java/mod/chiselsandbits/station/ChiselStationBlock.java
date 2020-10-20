package mod.chiselsandbits.station;

import mod.chiselsandbits.bitbag.BagContainer;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class ChiselStationBlock extends ContainerBlock
{

    public static final DirectionProperty FACING            = HorizontalBlock.HORIZONTAL_FACING;
    private static final VoxelShape     NORTH_SOUTH_SHAPE = Stream.of(
      Block.makeCuboidShape(0, 0, 0, 16, 5, 16),
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 5, 0.5, 14.5, 12.7, 2.5), Block.makeCuboidShape(6.950000000000001, 9.55, 2.5, 9.05, 11.65, 5.5), IBooleanFunction.OR),
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.5, 5, 13.5, 14.5, 12.7, 15.5), Block.makeCuboidShape(6.950000000000001, 9.55, 10.5, 9.05, 11.65, 13.5), IBooleanFunction.OR)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR)).orElse(VoxelShapes.empty());
    private static final VoxelShape EAST_WEST_SHAPE = Stream.of(
      Block.makeCuboidShape(0, 0, 0, 16, 5, 16),
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.5, 5, 1.5, 2.5, 12.7, 14.5), Block.makeCuboidShape(2.5, 9.55, 6.949999999999999, 5.5, 11.65, 9.049999999999997), IBooleanFunction.OR),
      VoxelShapes.combineAndSimplify(Block.makeCuboidShape(13.5, 5, 1.5, 15.5, 12.7, 14.5), Block.makeCuboidShape(10.5, 9.55, 6.949999999999999, 13.5, 11.65, 9.049999999999999), IBooleanFunction.OR)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR)).orElse(VoxelShapes.empty());

    public ChiselStationBlock(final Properties builder)
    {
        super(builder);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        return state.get(FACING).getAxis() == Direction.Axis.X ? NORTH_SOUTH_SHAPE : EAST_WEST_SHAPE;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final IBlockReader worldIn)
    {
        return new ChiselStationTileEntity();
    }

    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return ActionResultType.SUCCESS;
        } else {
            player.openContainer((INamedContainerProvider) worldIn.getTileEntity(pos));
            return ActionResultType.CONSUME;
        }
    }

    @Override
    public void addInformation(
      final ItemStack stack, @Nullable final IBlockReader worldIn, final List<ITextComponent> tooltip, final ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.ChiselStationHelp, tooltip );
    }
}
