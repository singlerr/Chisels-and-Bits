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
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings({"deprecation", "ConstantConditions"})
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

    @NotNull
    @Override
    public VoxelShape getRenderShape(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos)
    {
        markMethod();
        return null;
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(@NotNull final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos)
    {
        markMethod();
        return null;
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        markMethod();
        return null;
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(@NotNull final BlockState state, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos, @NotNull final ISelectionContext context)
    {
        markMethod();
        return null;
    }

    @Override
    public float getPlayerRelativeBlockHardness(@NotNull final BlockState state, @NotNull final PlayerEntity player, @NotNull final IBlockReader worldIn, @NotNull final BlockPos pos)
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

    @NotNull
    @Override
    public List<ItemStack> getDrops(@NotNull final BlockState state, @NotNull final LootContext.Builder builder)
    {
        markMethod();
        return Lists.newArrayList();
    }
}
