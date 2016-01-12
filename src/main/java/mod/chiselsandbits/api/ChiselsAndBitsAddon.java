package mod.chiselsandbits.api;

/**
 * Implement this on a class with the @ChiselsAndBitsPlugin, you can do anything
 * you want to get your support ready, such as store the object for later use,
 * or replace a null implementation with a C&B implementation.
 *
 * Implementing object must have a default constructor.
 */
public interface ChiselsAndBitsAddon
{

	/**
	 * Called during init-phase.
	 *
	 * @param api
	 */
	public void onReadyChiselsAndBits(
			final IChiselAndBitsAPI api );

}
