package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.cutting.operation.ICuttingOperation;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.modification.operation.MirrorOverAxisModificationOperation;
import mod.chiselsandbits.modification.operation.RotateAroundAxisModificationOperation;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistry;
import mod.chiselsandbits.platforms.core.registries.deferred.ICustomRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.core.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public final class ModCuttingOperation
{
    private static final Logger                                   LOGGER              = LogManager.getLogger();
    private static final ICustomRegistrar<ICuttingOperation> OPERATION_REGISTRAR = ICustomRegistrar.create(ICuttingOperation.class, Constants.MOD_ID);

    public static final Supplier<IChiselsAndBitsRegistry<ICuttingOperation>> REGISTRY_SUPPLIER = OPERATION_REGISTRAR.makeRegistry(
      IChiselsAndBitsRegistry.Builder::simple
    );

    private ModCuttingOperation()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModCuttingOperation. This is a utility class");
    }

    public static void onModConstruction()
    {
        LOGGER.info("Loaded modification operation configuration.");
    }

}
