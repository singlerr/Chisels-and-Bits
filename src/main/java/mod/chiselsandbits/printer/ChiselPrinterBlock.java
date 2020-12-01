package mod.chiselsandbits.printer;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class ChiselPrinterBlock extends ContainerBlock
{

    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    private static final VoxelShape       SHAPE  = Stream.of(
      Block.makeCuboidShape(0, 5, 0, 2, 16, 2),
      Block.makeCuboidShape(14, 5, 0, 16, 16, 2),
      Block.makeCuboidShape(0, 5, 14, 2, 16, 16),
      Block.makeCuboidShape(14, 5, 14, 16, 16, 16),
      Block.makeCuboidShape(2, 14, 0, 14, 16, 2),
      Block.makeCuboidShape(2, 14, 14, 14, 16, 16),
      Block.makeCuboidShape(0, 14, 2, 2, 16, 14),
      Block.makeCuboidShape(14, 14, 2, 16, 16, 14),
      Block.makeCuboidShape(0, 0, 0, 16, 5, 16),
      Block.makeCuboidShape(7, 1, -0.5, 12, 4, 0)
    ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).orElse(VoxelShapes.empty());

    public ChiselPrinterBlock(final Properties builder)
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
        return SHAPE;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final IBlockReader worldIn)
    {
        return new ChiselPrinterTileEntity();
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
