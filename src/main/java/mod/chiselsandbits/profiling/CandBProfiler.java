package mod.chiselsandbits.profiling;

import mod.chiselsandbits.api.profiling.IProfiler;
import mod.chiselsandbits.api.profiling.IProfilerResult;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.Util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CandBProfiler implements IProfiler
{

    /**
     * Creates a callback to pass to {@link ProfilingManager#withProfiler(Consumer)} to start a new section.
     *
     * @param name The name of the new section.
     * @return The callback.
     */
    public static Consumer<IProfiler> start(final String name) {
        return profiler -> profiler.startSection(name);
    }

    /**
     * Creates a callback to pass to {@link ProfilingManager#withProfiler(Consumer)} to end the current section and start a new section.
     *
     * @param name The name of the new section.
     * @return The callback.
     */
    public static Consumer<IProfiler> endStart(final String name) {
        return profiler -> profiler.endStartSection(name);
    }

    private final Profiler inner = new Profiler(Util.nanoTimeSupplier, () -> 0, false);

    public CandBProfiler()
    {
        inner.startTick();
    }

    @Override
    public void startSection(final String name)
    {
        inner.startSection(name);
    }

    @Override
    public void startSection(final Supplier<String> nameSupplier)
    {
        inner.startSection(nameSupplier);
    }

    @Override
    public void endSection()
    {
        inner.endSection();
    }

    public IProfilerResult getResult() {
        inner.endTick();
        final IProfilerResult result = new CandBProfilingResult(inner.getResults());
        inner.startTick();
        return result;
    }
}
