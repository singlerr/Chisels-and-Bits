package mod.chiselsandbits.api;

import mod.chiselsandbits.api.Exceptions.CannotBeChiseled;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface IChiselAndBitsAPI
{

	/**
	 * Check if a block can support {@link IBitAccess}
	 *
	 * @param world
	 * @param pos
	 * @return true if the block can be chiseled, this is true for air,
	 *         multiparts, and blocks which can be chisleled.
	 */
	boolean canBeChiseled(
			World world,
			BlockPos pos );

	/**
	 * is this block already chiseled?
	 *
	 * @param world
	 * @param pos
	 * @return true if the block contains chisled bits.
	 */
	boolean isBlockChiseled(
			World world,
			BlockPos pos );

	/**
	 * Get Access to the bits for a given block.
	 *
	 * @param world
	 * @param pos
	 * @return
	 * @throws CannotBeChiseled
	 */
	IBitAccess getBitAccess(
			World world,
			BlockPos pos ) throws CannotBeChiseled;

	/**
	 * Convert a hit pos and side into a bit pos.
	 *
	 * @param hitX
	 * @param hitY
	 * @param hitZ
	 * @param side
	 * @return
	 */
	BlockPos getBitPos(
			float hitX,
			float hitY,
			float hitZ,
			EnumFacing side );

}
