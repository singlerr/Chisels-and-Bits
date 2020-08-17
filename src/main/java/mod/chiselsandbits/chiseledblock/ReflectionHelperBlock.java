package mod.chiselsandbits.chiseledblock;

import com.google.common.collect.Lists;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

import java.util.List;

class ReflectionHelperBlock extends Block
{
	public String MethodName;

	private void markMethod()
	{
		MethodName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();
	}

	protected ReflectionHelperBlock()
	{
		super( AbstractBlock.Properties.create(Material.AIR) );
	}

    @Override
    public VoxelShape getRenderShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        markMethod();
        return null;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final IBlockReader reader, final BlockPos pos)
    {
        markMethod();
        return null;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        markMethod();
        return null;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        markMethod();
        return null;
    }

    @Override
    public float getPlayerRelativeBlockHardness(final BlockState state, final PlayerEntity player, final IBlockReader worldIn, final BlockPos pos)
    {
        markMethod();
        return 0;
    }

    @Override
    public float getExplosionResistance()
    {
        markMethod();
        return 0;
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final LootContext.Builder builder)
    {
        markMethod();
        return Lists.newArrayList();
    }
}