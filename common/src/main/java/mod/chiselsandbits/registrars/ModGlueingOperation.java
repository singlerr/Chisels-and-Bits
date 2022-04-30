package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.glueing.operation.IGlueingOperation;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistry;
import mod.chiselsandbits.platforms.core.registries.deferred.ICustomRegistrar;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public final class ModGlueingOperation
{
    private static final Logger                              LOGGER              = LogManager.getLogger();
    private static final ICustomRegistrar<IGlueingOperation> OPERATION_REGISTRAR = ICustomRegistrar.create(IGlueingOperation.class, Constants.MOD_ID);

    public static final Supplier<IChiselsAndBitsRegistry<IGlueingOperation>> REGISTRY_SUPPLIER = OPERATION_REGISTRAR.makeRegistry(
      IChiselsAndBitsRegistry.Builder::simple
    );

    private ModGlueingOperation()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModGlueingOperation. This is a utility class");
    }

    public static void onModConstruction()
    {
        LOGGER.info("Loaded modification operation configuration.");
    }

}
