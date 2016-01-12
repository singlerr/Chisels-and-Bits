package mod.chiselsandbits.api;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Do not implement, is passed to your {@link IChiselsAndBitsAddon}
 */
public interface IChiselAndBitsAPI
{

	/**
	 * Is item a Chisel?
	 *
	 * @param item
	 * @return true if yes.
	 */
	boolean isChisel(
			ItemStack item );

	/**
	 * Is item a bit bag?
	 *
	 * @param item
	 * @return true if yes.
	 */
	boolean isBitBag(
			ItemStack item );

	/**
	 * Is the item a chiseled bit?
	 *
	 * @param item
	 * @return true if yes.
	 */
	boolean isBitItem(
			ItemStack item );

	/**
	 * Is the item a chiseled bit block?
	 *
	 * @param item
	 * @return true if yes.
	 */
	boolean isChisledBitBlock(
			ItemStack item );

	/**
	 * Check if a block can support {@link IBitAccess}
	 *
	 * @param world
	 * @param pos
	 * @return true if the block can be chiseled, this is true for air,
	 *         multi-parts, and blocks which can be chiseled, false otherwise.
	 */
	boolean canBeChiseled(
			World world,
			BlockPos pos );

	/**
	 * is this block already chiseled?
	 *
	 * @param world
	 * @param pos
	 * @return true if the block contains chiseled bits, false otherwise.
	 */
	boolean isBlockChiseled(
			World world,
			BlockPos pos );

	/**
	 * Get Access to the bits for a given block.
	 *
	 * @param world
	 * @param pos
	 * @return A {@link IBitAccess} for the specified location.
	 * @throws CannotBeChiseled
	 *             when the location cannot support bits, or if the parameters
	 *             are invalid.
	 */
	IBitAccess getBitAccess(
			World world,
			BlockPos pos ) throws CannotBeChiseled;

	/**
	 * Create a bit access from an item, passing null creates an empty item.
	 *
	 * @return a {@link IBitAccess} for an item.
	 */
	IBitAccess createBitItem(
			ItemStack BitItemStack );

	/**
	 * Create a brush from an item, once created you can use it many times.
	 *
	 * @param bitItem
	 * @return A brush for the specified item, if null is passed for the item an
	 *         air brush is created.
	 * @throws InvalidBitItem
	 */
	IBitBrush createBrush(
			ItemStack bitItem ) throws InvalidBitItem;

	/**
	 * Convert ray trace information into bit location information
	 *
	 * @param hitX
	 * @param hitY
	 * @param hitZ
	 * @param side
	 * @param pos
	 * @param placement
	 * @return details about the target bit, if any parameters are missing will
	 *         return null.
	 */
	IBitLocation getBitPos(
			float hitX,
			float hitY,
			float hitZ,
			EnumFacing side,
			BlockPos pos,
			boolean placement );

}
