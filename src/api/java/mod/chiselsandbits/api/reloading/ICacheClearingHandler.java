package mod.chiselsandbits.api.reloading;

/**
 * Invoked by C&Bs reloading handler.
 */
@FunctionalInterface
public interface ICacheClearingHandler
{
    /**
     * Invoked when the cache needs to be cleared.
     */
    void clear();
}
