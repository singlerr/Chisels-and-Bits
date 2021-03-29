package mod.chiselsandbits.api.block;

import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;

/**
 * Implemented by C&B Blocks, can be used to request a material that represents
 * the largest quantity of a C&B block.
 */
public interface IMultiStateBlock
{

    /**
     * Returns the primary state of the block.
     * In terms of C&B this is the blockstate that is represented the most inside the block.
     *
     * @param world The world to read the data from. Required to get access to the tile entity with the backing data.
     * @param pos The pos in the given world to get the primary blockstate from. Required to get access to the tile entity with the backing data.
     * @return The primary blockstate, or when not found the default state from the air block. {@link AirBlock#getDefaultState()}
     */
    @NotNull
	BlockState getPrimaryState(
			@NotNull IBlockReader world,
			@NotNull BlockPos pos );
}
