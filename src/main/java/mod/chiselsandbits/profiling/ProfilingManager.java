package mod.chiselsandbits.profiling;

import mod.chiselsandbits.api.profiling.IProfiler;
import mod.chiselsandbits.api.profiling.IProfilerResult;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.api.profiling.IProfilingManager;

import java.util.function.Consumer;

public class ProfilingManager implements IProfilingManager
{
    private static final ProfilingManager INSTANCE = new ProfilingManager();

    public static ProfilingManager getInstance()
    {
        return INSTANCE;
    }

    public IProfiler profiler = null;

    private ProfilingManager()
    {
    }

    @Override
    public IProfiler startProfiling()
    {
        return new CandBProfiler();
    }

    @Override
    public IProfilerResult endProfiling(final IProfiler profiler)
    {
        if (!(profiler instanceof CandBProfiler))
            throw new IllegalArgumentException("Profiler is not a C&B Profiler");

        final CandBProfiler candBProfiler = (CandBProfiler) profiler;

        return candBProfiler.getResult();
    }

    public IProfiler getProfiler()
    {
        return profiler;
    }

    public void setProfiler(final IProfiler profiler)
    {
        this.profiler = profiler;
    }

    public boolean hasProfiler() {
        return getProfiler() != null;
    }

    public void withProfiler(final Consumer<IProfiler> callback) {
        if (hasProfiler())
            callback.accept(getProfiler());
    }

    public IProfilerSection withSection(final String name) {
        final IProfiler profiler = getProfiler();

        if (profiler != null)
            profiler.startSection(name);

        return () -> {
            if (profiler != null)
                profiler.endSection();
        };
    }
}
