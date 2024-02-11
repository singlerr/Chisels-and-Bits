package mod.chiselsandbits.forge.data.model;

import com.communi.suggestu.scena.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Objects;

public abstract class AbstractInteractableItemModelGenerator extends ItemModelProvider {
    private final IRegistryObject<? extends Item> targetRegistryObject;

    protected AbstractInteractableItemModelGenerator(
            final DataGenerator generator,
            final ExistingFileHelper existingFileHelper,
            final IRegistryObject<? extends Item> targetRegistryObject) {
        super(generator.getPackOutput(), Constants.MOD_ID, existingFileHelper);
        this.targetRegistryObject = targetRegistryObject;
    }

    @Override
    protected void registerModels() {
        getBuilder(
                Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(this.targetRegistryObject.get())).getPath()
        )
                .parent(
                        getExistingFile(new ResourceLocation(new ResourceLocation(Constants.MOD_ID, "item/" + Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(this.targetRegistryObject.get())).getPath()) + "_spec"))
                )
                .customLoader((itemModelBuilder, existingFileHelper) -> new CustomLoaderBuilder<>(
                        new ResourceLocation(Constants.INTERACTABLE_MODEL_LOADER),
                        itemModelBuilder,
                        existingFileHelper,
                        false
                ) {
                });
    }
}
