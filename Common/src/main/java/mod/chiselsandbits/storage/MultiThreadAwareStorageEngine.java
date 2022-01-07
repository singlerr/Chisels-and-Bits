package mod.chiselsandbits.storage;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.config.ICommonConfiguration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

final class MultiThreadAwareStorageEngine implements IMultiThreadedStorageEngine
{

    private static ExecutorService saveService;

    private static synchronized void ensureThreadPoolSetup() {
        if (saveService == null) {
            final ClassLoader classLoader = ChiselsAndBits.class.getClassLoader();
            final AtomicInteger genericThreadCounter = new AtomicInteger();
            saveService = Executors.newFixedThreadPool(
              ICommonConfiguration.getInstance().getBlockSaveThreadCount().get(),
              runnable -> {
                  final Thread thread = new Thread(runnable);
                  thread.setContextClassLoader(classLoader);
                  thread.setName(String.format("Chisels and Bits Block save handler #%s", genericThreadCounter.incrementAndGet()));
                  thread.setDaemon(true);
                  return thread;
              }
            );
        }
    }

    private final IStorageEngine internalEngine;

    MultiThreadAwareStorageEngine(final IStorageEngine internalEngine) {this.internalEngine = internalEngine;}

    @Override
    public void serializeNBTInto(final CompoundTag tag)
    {
        internalEngine.serializeNBTInto(tag);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return internalEngine.serializeNBT();
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        internalEngine.deserializeNBT(nbt);
    }

    @Override
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {
        internalEngine.serializeInto(packetBuffer);
    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {
        internalEngine.deserializeFrom(packetBuffer);
    }

    @Override
    public Collection<? extends IStorageHandler> getHandlers()
    {
        return internalEngine.getHandlers();
    }

    @Override
    public CompletableFuture<CompoundTag> serializeOffThread()
    {
        ensureThreadPoolSetup();
        return CompletableFuture.supplyAsync(
          this::serializeNBT,
          saveService
        );
    }
}
