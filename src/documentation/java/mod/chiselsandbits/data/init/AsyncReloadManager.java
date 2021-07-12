package mod.chiselsandbits.data.init;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.AsyncReloader;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class AsyncReloadManager extends ThreadTaskExecutor<Runnable>
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

    public void reload(IResourceManager resourceManager, IFutureReloadListener reloadListener) {
        final IAsyncReloader asyncLoader
          = AsyncReloader.create(resourceManager, Lists.newArrayList(reloadListener), Runnable::run, Runnable::run, CompletableFuture.completedFuture(Unit.INSTANCE));

        try
        {
            while(!asyncLoader.fullyDone()) {
                if (!this.driveOne()) {
                    this.threadYieldPark();
                }
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Failed to run an async reload.");
        }
    }

    @Override
    protected @NotNull Runnable wrapTask(final @NotNull Runnable runnable)
    {
        return runnable;
    }

    @Override
    protected boolean canRun(final @NotNull Runnable runnable)
    {
        return true;
    }

    @Override
    protected @NotNull Thread getExecutionThread()
    {
        return Thread.currentThread();
    }
}
