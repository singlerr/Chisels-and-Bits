package mod.chiselsandbits.data.init;

import com.google.common.collect.Lists;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.BlockableEventLoop;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AsyncReloadManager extends BlockableEventLoop<Runnable>
{
    private static final AsyncReloadManager INSTANCE = new AsyncReloadManager();

    public static AsyncReloadManager getInstance()
    {
        return INSTANCE;
    }

    private AsyncReloadManager()
    {
        super("C&B - Data Driven Runner");
    }

    public void reload(ResourceManager resourceManager, PreparableReloadListener reloadListener) {
        final ReloadInstance asyncLoader
          = SimpleReloadInstance.of(resourceManager, Lists.newArrayList(reloadListener), Runnable::run, Runnable::run, CompletableFuture.completedFuture(Unit.INSTANCE));

        try
        {
            while(!asyncLoader.isDone()) {
                if (!this.pollTask()) {
                    this.waitForTasks();
                }
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Failed to run an async reload.");
        }
    }

    @Override
    protected @NotNull Runnable wrapRunnable(final @NotNull Runnable runnable)
    {
        return runnable;
    }

    @Override
    protected boolean shouldRun(final @NotNull Runnable runnable)
    {
        return true;
    }

    @Override
    protected @NotNull Thread getRunningThread()
    {
        return Thread.currentThread();
    }
}
