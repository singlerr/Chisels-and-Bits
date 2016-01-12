package mod.chiselsandbits.api;

import mod.chiselsandbits.api.Exceptions.InvalidVoxelItem;
import mod.chiselsandbits.api.Exceptions.SpaceOccupied;
import net.minecraft.item.ItemStack;

public interface IBitAccess
{

	/**
	 * Returns the bit at the specific location, null is air.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	ItemStack getBitItemAt(
			int x,
			int y,
			int z );

	/**
	 * Sets the bit at the specific location, null is air.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param bit
	 * @throws InvalidVoxelItem
	 */
	void setBitItemAt(
			int x,
			int y,
			int z,
			ItemStack bit ) throws InvalidVoxelItem, SpaceOccupied;

	/**
	 * Any time you modify a block you must commit your changes for them to take
	 * affect.
	 */
	void commitChanges();

}
