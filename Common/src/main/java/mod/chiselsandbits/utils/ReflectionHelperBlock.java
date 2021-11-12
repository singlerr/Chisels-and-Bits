package mod.chiselsandbits.utils;

import com.google.common.collect.Lists;
import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class ReflectionHelperBlock extends Block implements IBlockWithWorldlyProperties
{
    public String MethodName;

    private void markMethod()
    {
        MethodName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();
    }

    public ReflectionHelperBlock()
    {
        super( Properties.of(Material.AIR) );
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

    @Override
    public float getFriction(BlockState state, LevelReader levelReader, BlockPos pos, @Nullable Entity entity) {
        markMethod();
        return 0f;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        markMethod();
        return 0;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter blockGetter, BlockPos pos, Player player) {
        markMethod();
        return false;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter blockGetter, BlockPos pos, Player player) {
        markMethod();
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor levelAccessor, BlockPos pos, Rotation rotation) {
        markMethod();
        return null;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, LevelReader levelReader, BlockPos pos, Direction side) {
        markMethod();
        return false;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter blockAndTintGetter, BlockPos pos, FluidState fluidState) {
        markMethod();
        return false;
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader levelReader, BlockPos pos, BlockPos beaconPos) {
        markMethod();
        return new float[0];
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader levelReader, BlockPos pos, @Nullable Entity entity) {
        markMethod();
        return null;
    }

    @Override
    public float getExplosionResistance(final BlockState state, final BlockGetter world, final BlockPos pos, final Explosion explosion)
    {
        markMethod();
        return 0f;
    }

    @Nullable
    @Override
    public List<ItemStack> getDrops(@Nullable final BlockState state, @Nullable final LootContext.Builder builder)
    {
        markMethod();
        return Lists.newArrayList();
    }
}
