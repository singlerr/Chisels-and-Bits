package mod.chiselsandbits.api.profiling;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;

/**
 * A profiling manager to handle the profiling of different interactions or managements.
 */
public interface IProfilingManager
{

    /**
     * Gives access to the current profiling manager.
     *
     * @return The current profiling manager.
     */
    static IProfilingManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getProfilingManager();
    }

    /**
     * Creates a new profiler.
     *
     * @return The profiler
     */
    IProfiler startProfiling();

    /**
     * Ends the profiling of a given profiler
     * @param profiler The profiler to end profiling.
     *
     * @return The result of the profiling.
     */
    IProfilerResult endProfiling(IProfiler profiler);

}
