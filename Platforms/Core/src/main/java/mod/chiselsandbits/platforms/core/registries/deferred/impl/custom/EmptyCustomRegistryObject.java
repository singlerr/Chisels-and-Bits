package mod.chiselsandbits.platforms.core.registries.deferred.impl.custom;

import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EmptyCustomRegistryObject<T> implements IRegistryObject<T>
{
    @Override
    public @NotNull T get()
    {
        return null;
    }

    @Override
    public ResourceLocation getId()
    {
        return new ResourceLocation(Constants.MOD_ID, "<!!!EMPTY!!!>");
    }

    @Override
    public Stream<T> stream()
    {
        return Stream.of();
    }

    @Override
    public boolean isPresent()
    {
        return false;
    }

    @Override
    public void ifPresent(final Consumer<? super T> consumer)
    {

    }

    @Override
    public IRegistryObject<T> filter(final Predicate<? super T> predicate)
    {
        return this;
    }

    @Override
    public <U> Optional<U> map(final Function<? super T, ? extends U> mapper)
    {
        return Optional.empty();
    }

    @Override
    public <U> Optional<U> flatMap(final Function<? super T, Optional<U>> mapper)
    {
        return Optional.empty();
    }

    @Override
    public <U> Supplier<U> lazyMap(final Function<? super T, ? extends U> mapper)
    {
        return () -> null;
    }

    @Override
    public T orElse(final T other)
    {
        return other;
    }

    @Override
    public T orElseGet(final Supplier<? extends T> other)
    {
        return other.get();
    }

    @Override
    public <X extends Throwable> T orElseThrow(final Supplier<? extends X> exceptionSupplier) throws X
    {
        throw exceptionSupplier.get();
    }
}
