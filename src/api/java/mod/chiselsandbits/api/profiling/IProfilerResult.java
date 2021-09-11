package mod.chiselsandbits.api.profiling;

import net.minecraft.profiler.DataPoint;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * The result of a profiling operation.
 */
public interface IProfilerResult
{
    /**
     * Writes all profiling result data to the given file. Creating it if necessary.
     * @param file The file to write to.
     */
    void writeToFile(File file);

    /**
     * Writes all profiling result data in a readable manor into the given consumer, allows for the outputting of the results to a player in chat, or the server console.
     * @param lineConsumer The line consumer.
     */
    void writeAsResponse(final Consumer<String> lineConsumer);
}
