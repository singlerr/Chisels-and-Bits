package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.pattern.placement.IPatternPlacementType;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.modification.operation.MirrorOverAxisModificationOperation;
import mod.chiselsandbits.modification.operation.RotateAroundAxisModificationOperation;
import mod.chiselsandbits.pattern.placement.*;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class ModPatternPlacementTypes
{
    private static final DeferredRegister<IPatternPlacementType> TYPE_REGISTRAR = DeferredRegister.create(IPatternPlacementType.class, Constants.MOD_ID);

    public static final Supplier<IForgeRegistry<IPatternPlacementType>>
      REGISTRY_SUPPLIER = TYPE_REGISTRAR.makeRegistry("pattern_placement", () -> new RegistryBuilder<IPatternPlacementType>()
      .allowModification()
      .disableOverrides()
      .disableSaving()
    );

    public static final RegistryObject<IPatternPlacementType> PLACEMENT = TYPE_REGISTRAR.register(
      "placement", PlacePatternPlacementType::new
    );
    public static final RegistryObject<IPatternPlacementType> REMOVAL = TYPE_REGISTRAR.register(
      "removal", RemovalPatternPlacementType::new
    );
    public static final RegistryObject<IPatternPlacementType> IMPOSEMENT = TYPE_REGISTRAR.register(
      "imposement", ImposePatternPlacementType::new
    );
    public static final RegistryObject<IPatternPlacementType> MERGE = TYPE_REGISTRAR.register(
      "merge", MergePatternPlacementType::new
    );
    public static final RegistryObject<IPatternPlacementType> CARVING = TYPE_REGISTRAR.register(
      "carving", CarvePatternPlacementType::new
    );

    private ModPatternPlacementTypes()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModPatternPlacementTypes. This is a utility class");
    }

    public static void onModConstruction() {
        TYPE_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
