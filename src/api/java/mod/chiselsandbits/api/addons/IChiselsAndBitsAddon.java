package mod.chiselsandbits.api.addons;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;

/**
 * Implement this on a class with the @ChiselsAndBitsAddon annotation, you can
 * do anything you want to get your support ready inside the callback, such as
 * store the object for later use, or replace a null implementation with a Chisels and Bits
 * implementation.
 *
 * Implementing object must be public, and have a public default constructor and
 * have the @ChiselsAndBitsAddon annotation, failure to do these steps will
 * result in an error.
 */
public interface IChiselsAndBitsAddon
{
	/**
	 * Called during init-phase for Chisels and Bits.
	 *
	 * @param api The api of chisels and bits.
	 */
	void commonSetup(final IChiselsAndBitsAPI api);
}
