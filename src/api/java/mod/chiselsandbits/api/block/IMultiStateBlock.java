package mod.chiselsandbits.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Implemented by C&B Blocks, can be used to request a material that represents
 * the largest quantity of a C&B block.
 */
public interface IMultiStateBlock extends EntityBlock
{

    /**
     * Returns the primary state of the block.
     * In terms of C&B this is the blockstate that is represented the most inside the block.
     *
     * @param world The world to read the data from. Required to get access to the tile entity with the backing data.
     * @param pos The pos in the given world to get the primary blockstate from. Required to get access to the tile entity with the backing data.
     * @return The primary blockstate, or when not found the default state from the air block. {@link AirBlock#defaultBlockState()}
     */
    @NotNull
	BlockState getPrimaryState(
			@NotNull BlockGetter world,
			@NotNull BlockPos pos );
}
