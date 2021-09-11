package mod.chiselsandbits.profiling;

import mod.chiselsandbits.api.profiling.IProfilerResult;
import net.minecraft.profiler.DataPoint;
import net.minecraft.profiler.FilledProfileResult;
import net.minecraft.profiler.IProfileResult;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CandBProfilingResult implements IProfilerResult
{
    private final IProfileResult innerResult;

    public CandBProfilingResult(final IProfileResult innerResult) {this.innerResult = innerResult;}

    @Override
    public void writeToFile(final File file)
    {
        innerResult.writeToFile(file);
    }

    @Override
    public void writeAsResponse(final Consumer<String> lineConsumer)
    {
        if (!(innerResult instanceof FilledProfileResult))
            return;

        final FilledProfileResult filledProfileResult = (FilledProfileResult) innerResult;
        lineConsumer.accept("Results:");
        writeDatapointsAsResponse("root", filledProfileResult::getDataPoints, lineConsumer, " ");
    }

    private void writeDatapointsAsResponse(final String name, final Function<String, List<DataPoint>> producer, final Consumer<String> lineConsumer, final String indent) {
        final List<DataPoint> dataPoints = producer.apply(name);

        dataPoints.forEach(
          dataPoint -> {
              lineConsumer.accept(String.format("%s> %s: %s (%s)", indent, dataPoint.name.substring(dataPoint.name.lastIndexOf('\u001e') + 1), dataPoint.relTime, dataPoint.rootRelTime));

              if (!dataPoint.name.equals(name)) {
                  writeDatapointsAsResponse(name + '\u001e' + dataPoint.name, producer, lineConsumer, indent + " ");
              }
          }
        );
    }
}
