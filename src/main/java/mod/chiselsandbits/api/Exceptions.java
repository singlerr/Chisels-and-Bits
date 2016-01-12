package mod.chiselsandbits.api;

public class Exceptions
{

	/**
	 * Thrown when a ItemStack is not a valid chiseled bit.
	 */
	static public class InvalidVoxelItem extends Exception
	{

		private static final long serialVersionUID = -1101206778339096174L;

	}

	/**
	 * Thrown When a block cannot hold chiseled bits, or be converted to a
	 * chiseled block.
	 */
	static public class CannotBeChiseled extends Exception
	{

		private static final long serialVersionUID = -2164869172949828669L;

	}

	/**
	 * Thrown when a bit cannot be placed in that space, this is not caused by
	 * bits already occupying that space, but by multiparts.
	 */
	static public class SpaceOccupied extends Exception
	{

		private static final long serialVersionUID = -2164869172949828669L;

	}

}
