package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.modification.operation.MirrorOverAxisModificationOperation;
import mod.chiselsandbits.modification.operation.RotateAroundAxisModificationOperation;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistry;
import mod.chiselsandbits.platforms.core.registries.deferred.ICustomRegistrar;
import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import net.minecraft.core.Direction;

import java.util.function.Supplier;

public final class ModModificationOperation
{
    private static final ICustomRegistrar<IModificationOperation> OPERATION_REGISTRAR = ICustomRegistrar.create(IModificationOperation.class, Constants.MOD_ID);

    public static final Supplier<IChiselsAndBitsRegistry<IModificationOperation>> REGISTRY_SUPPLIER = OPERATION_REGISTRAR.makeRegistry("modification_operation",
      IChiselsAndBitsRegistry.Builder::simple);
    
    public static final IRegistryObject<IModificationOperation> ROTATE_AROUND_X = OPERATION_REGISTRAR.register(
      "rotate_around_x", () -> RotateAroundAxisModificationOperation.Builder.create().withAxis(Direction.Axis.X).build()
    );
    public static final IRegistryObject<IModificationOperation> ROTATE_AROUND_Y = OPERATION_REGISTRAR.register(
      "rotate_around_y", () -> RotateAroundAxisModificationOperation.Builder.create().withAxis(Direction.Axis.Y).build()
    );
    public static final IRegistryObject<IModificationOperation> ROTATE_AROUND_Z = OPERATION_REGISTRAR.register(
      "rotate_around_z", () -> RotateAroundAxisModificationOperation.Builder.create().withAxis(Direction.Axis.Z).build()
    );
    public static final IRegistryObject<IModificationOperation> MIRROR_OVER_X   = OPERATION_REGISTRAR.register(
      "mirror_over_x", () -> MirrorOverAxisModificationOperation.Builder.create().withAxis(Direction.Axis.X).build()
    );
    public static final IRegistryObject<IModificationOperation> MIRROR_OVER_Y   = OPERATION_REGISTRAR.register(
      "mirror_over_y", () -> MirrorOverAxisModificationOperation.Builder.create().withAxis(Direction.Axis.Y).build()
    );
    public static final IRegistryObject<IModificationOperation> MIRROR_OVER_Z   = OPERATION_REGISTRAR.register(
      "mirror_over_z", () -> MirrorOverAxisModificationOperation.Builder.create().withAxis(Direction.Axis.Z).build()
    );

    private ModModificationOperation()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModModificationOperation. This is a utility class");
    }
}
