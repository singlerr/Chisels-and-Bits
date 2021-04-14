package mod.chiselsandbits.utils;

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
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class ReflectionHelperBlock extends Block
{
    public String MethodName;

    private void markMethod()
    {
        MethodName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();
    }

    public ReflectionHelperBlock()
    {
        super( AbstractBlock.Properties.create(Material.AIR) );
    }

    @Nullable
    @Override
    public VoxelShape getRenderShape(@Nullable final BlockState state, @Nullable final IBlockReader worldIn, @Nullable final BlockPos pos)
    {
        markMethod();
        return null;
    }

    @Nullable
    @Override
    public VoxelShape getCollisionShape(@Nullable final BlockState state, @Nullable final IBlockReader reader, @Nullable final BlockPos pos)
    {
        markMethod();
        return null;
    }

    @Nullable
    @Override
    public VoxelShape getShape(@Nullable final BlockState state, @Nullable final IBlockReader worldIn, @Nullable final BlockPos pos, @Nullable final ISelectionContext context)
    {
        markMethod();
        return null;
    }

    @Nullable
    @Override
    public VoxelShape getCollisionShape(@Nullable final BlockState state, @Nullable final IBlockReader worldIn, @Nullable final BlockPos pos, @Nullable final ISelectionContext context)
    {
        markMethod();
        return null;
    }

    @Override
    public float getPlayerRelativeBlockHardness(@Nullable final BlockState state, @Nullable final PlayerEntity player, @Nullable final IBlockReader worldIn, @Nullable final BlockPos pos)
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

    @Nullable
    @Override
    public List<ItemStack> getDrops(@Nullable final BlockState state, @Nullable final LootContext.Builder builder)
    {
        markMethod();
        return Lists.newArrayList();
    }
}
