package mod.chiselsandbits.platforms.core.util;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class LambdaExceptionUtils {

    /**
     * .forEach(rethrowConsumer(name -> System.out.println(Class.forName(name)))); or .forEach(rethrowConsumer(ClassNameUtil::println));
     */
    public static <T, E extends Exception> Consumer<T> rethrowConsumer(Consumer_WithExceptions<T, E> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
            }
        };
    }

    private static <E extends Throwable> void throwAsUnchecked(Exception exception) throws E {
        throw (E) exception;
    }

    @FunctionalInterface
    public interface Consumer_WithExceptions<T, E extends Exception> {
        void accept(T t) throws E;
    }
}
