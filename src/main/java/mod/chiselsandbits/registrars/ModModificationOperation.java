package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.modification.operation.IModificationTableOperation;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.modification.operation.MirrorOverAxisModificationOperation;
import mod.chiselsandbits.modification.operation.RotateAroundAxisModificationOperation;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class ModModificationOperation
{
    private static final DeferredRegister<IModificationTableOperation> OPERATION_REGISTRAR = DeferredRegister.create(IModificationTableOperation.class, Constants.MOD_ID);

    public static final Supplier<IForgeRegistry<IModificationTableOperation>> REGISTRY_SUPPLIER = OPERATION_REGISTRAR.makeRegistry("modification_operation", () -> new RegistryBuilder<IModificationTableOperation>()
      .allowModification()
      .disableOverrides()
      .disableSaving()
    );
    
    public static final RegistryObject<IModificationTableOperation> ROTATE_AROUND_X = OPERATION_REGISTRAR.register(
      "rotate_around_x", () -> RotateAroundAxisModificationOperation.Builder.create().withAxis(Direction.Axis.X).build()
    );
    public static final RegistryObject<IModificationTableOperation> ROTATE_AROUND_Y = OPERATION_REGISTRAR.register(
      "rotate_around_y", () -> RotateAroundAxisModificationOperation.Builder.create().withAxis(Direction.Axis.Y).build()
    );
    public static final RegistryObject<IModificationTableOperation> ROTATE_AROUND_Z = OPERATION_REGISTRAR.register(
      "rotate_around_z", () -> RotateAroundAxisModificationOperation.Builder.create().withAxis(Direction.Axis.Z).build()
    );
    public static final RegistryObject<IModificationTableOperation> MIRROR_OVER_X = OPERATION_REGISTRAR.register(
      "mirror_over_x", () -> MirrorOverAxisModificationOperation.Builder.create().withAxis(Direction.Axis.X).build()
    );
    public static final RegistryObject<IModificationTableOperation> MIRROR_OVER_Y = OPERATION_REGISTRAR.register(
      "mirror_over_y", () -> MirrorOverAxisModificationOperation.Builder.create().withAxis(Direction.Axis.Y).build()
    );
    public static final RegistryObject<IModificationTableOperation> MIRROR_OVER_Z = OPERATION_REGISTRAR.register(
      "mirror_over_z", () -> MirrorOverAxisModificationOperation.Builder.create().withAxis(Direction.Axis.Z).build()
    );

    private ModModificationOperation()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModModificationOperation. This is a utility class");
    }

    public static void onModConstruction() {
        OPERATION_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
