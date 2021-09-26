package mod.chiselsandbits.utils;

import com.google.common.collect.Lists;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
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
        super( BlockBehaviour.Properties.of(Material.AIR) );
    }

    @Nullable
    @Override
    public VoxelShape getOcclusionShape(@Nullable final BlockState state, @Nullable final BlockGetter worldIn, @Nullable final BlockPos pos)
    {
        markMethod();
        return null;
    }

    @Nullable
    @Override
    public VoxelShape getBlockSupportShape(@Nullable final BlockState state, @Nullable final BlockGetter reader, @Nullable final BlockPos pos)
    {
        markMethod();
        return null;
    }

    @Nullable
    @Override
    public VoxelShape getShape(@Nullable final BlockState state, @Nullable final BlockGetter worldIn, @Nullable final BlockPos pos, @Nullable final CollisionContext context)
    {
        markMethod();
        return null;
    }

    @Nullable
    @Override
    public VoxelShape getCollisionShape(@Nullable final BlockState state, @Nullable final BlockGetter worldIn, @Nullable final BlockPos pos, @Nullable final CollisionContext context)
    {
        markMethod();
        return null;
    }

    @Override
    public float getDestroyProgress(@Nullable final BlockState state, @Nullable final Player player, @Nullable final BlockGetter worldIn, @Nullable final BlockPos pos)
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
