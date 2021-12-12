package mod.chiselsandbits.fabric.mixin.platform.world.entity.vehicle;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Boat.class, priority = Integer.MIN_VALUE)
public abstract class BoatWorldlyBlockMixin extends Entity
{

    public BoatWorldlyBlockMixin(final EntityType<?> entityType, final Level level)
    {
        super(entityType, level);
    }

    /**
     * @author Chisels & Bits
     * @reason It is not possible to properly inject the callback into the getFriction() call, while also capturing the required data.
     */
    @Overwrite
    public float getGroundFriction()
    {
        AABB myBoundingBox = this.getBoundingBox();
        AABB boxToCheck = new AABB(myBoundingBox.minX, myBoundingBox.minY - 0.001D, myBoundingBox.minZ, myBoundingBox.maxX, myBoundingBox.minY, myBoundingBox.maxZ);
        int minimalX = Mth.floor(boxToCheck.minX) - 1;
        int maximalX = Mth.ceil(boxToCheck.maxX) + 1;
        int minimalY = Mth.floor(boxToCheck.minY) - 1;
        int maximalY = Mth.ceil(boxToCheck.maxY) + 1;
        int minimalZ = Mth.floor(boxToCheck.minZ) - 1;
        int maximalZ = Mth.ceil(boxToCheck.maxZ) + 1;
        VoxelShape shapeOfBoxToCheck = Shapes.create(boxToCheck);
        float totalFriction = 0.0F;
        int checkedBlocks = 0;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int x = minimalX; x < maximalX; ++x)
        {
            for (int z = minimalZ; z < maximalZ; ++z)
            {
                int maximalOffset = (x != minimalX && x != maximalX - 1 ? 0 : 1) + (z != minimalZ && z != maximalZ - 1 ? 0 : 1);
                if (maximalOffset != 2)
                {
                    for (int y = minimalY; y < maximalY; ++y)
                    {
                        if (maximalOffset <= 0 || y != minimalY && y != maximalY - 1)
                        {
                            blockPos.set(x, y, z);
                            BlockState blockState = this.level.getBlockState(blockPos);
                            if (!(blockState.getBlock() instanceof WaterlilyBlock) && Shapes.joinIsNotEmpty(blockState.getCollisionShape(this.level, blockPos)
                              .move(x, y, z), shapeOfBoxToCheck, BooleanOp.AND))
                            {
                                if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties) {
                                    totalFriction += blockWithWorldlyProperties.getFriction(blockState, this.level, blockPos, this);
                                }
                                else
                                {
                                    totalFriction += blockState.getBlock().getFriction();
                                }

                                ++checkedBlocks;
                            }
                        }
                    }
                }
            }
        }

        return totalFriction / (float) checkedBlocks;
    }
}
