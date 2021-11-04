package mod.chiselsandbits.platforms.core.blockstate;

import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import org.jetbrains.annotations.Nullable;

/**
 * Allows access to game objects based on the world position.
 * Not every platform supports this natively, so we sometimes have to use different tactics to implement this.
 */
public interface ILevelBasedPropertyAccessor
{

    /**
     * Gives access to level based property accessors.
     *
     * @return The accessor for level based properties.
     */

    static ILevelBasedPropertyAccessor getInstance() {
        return IChiselsAndBitsPlatformCore.getInstance().getLevelBasedPropertyAccessor();
    }

    /**
     * Indicates if the blockstate on the given position should check for weak power propagation.
     *
     * @param levelReader The level reader to read the properties from.
     * @param blockPos The position to get the blockstate from out of the level reader for the check.0
     * @param direction The direction to check from.
     * @return True when the block propagates weak power from the given direction in the given level reader.
     */
    boolean shouldCheckWeakPower(LevelReader levelReader, BlockPos blockPos, Direction direction);

    /**
     * Determines the friction value of the blockstate on the given position in the given world reader.
     *
     * @param levelReader The world reader to get the blockstate from.
     * @param blockPos The position to get the friction of.
     * @param entity The entity to get the friction for. Might be null if unknown.
     * @return The friction value.
     */
    float getFriction(LevelReader levelReader, BlockPos blockPos, @Nullable Entity entity);

    /**
     * Determines the light emission value (so the amount of light emitted from the block itself) based on the blockstate on the given
     * position in the given reader.
     *
     * @param levelReader The level reader to pull the emission value from.
     * @param blockPos The position to read the value for.
     * @return The light emission value between 0 and 15
     */
    int getLightEmission(LevelReader levelReader, BlockPos blockPos);

    /**
     * Determines if the blockstate at the given position in the given block getter is able to propagate skylight downwards.
     * In other words if this method returns false then the block on the given position blocks skylight.
     *
     * @param blockGetter The block getter to get the blockstate from to determine the propagation.
     * @param blockPos The position of the block to check.
     * @return True when the block propagates skylight (like leaves and air) false when not (like stone)
     */
    default boolean propagatesSkylightDown(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos).propagatesSkylightDown(blockGetter, blockPos);
    }

    /**
     * Determines if the player can harvest the block in the block getter at the given position.
     *
     * @param blockGetter The block getter to check from.
     * @param pos The position of the block that is about to be harvested by the player.
     * @param player The player in question.
     * @return True when harvestable, false when not.
     */
    boolean canHarvestBlock(BlockGetter blockGetter, BlockPos pos, Player player);

    /**
     * Returns the beacon color multiplier of the blockstate on the given position in the block getter for the given beacon position.
     *
     * @param blockGetter The block getter to pull the multiplier from.
     * @param pos The position to get the blockstate from.
     * @param beaconPos The position of the beacon.
     * @return The color multiplier for the beacon.
     */
    float[] getBeaconColorMultiplier(BlockGetter blockGetter, BlockPos pos, BlockPos beaconPos);

    /**
     * Returns the sound type of the blockstate on the given position for the given entity.
     *
     * @param blockGetter The block getter to pull the blockstate out of.
     * @param pos The position to get the sound type for.
     * @param entity The entity to get the sound type for.
     * @return The sound type of the blockstate on the given position for the given entity.
     */
    SoundType getSoundType(BlockGetter blockGetter, BlockPos pos, Entity entity);
}
