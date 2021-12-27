package mod.chiselsandbits.forge.data.model;

import mod.chiselsandbits.platforms.core.registries.deferred.IRegistryObject;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

public abstract class AbstractInteractableItemModelGenerator extends ItemModelProvider
{
    private final IRegistryObject<? extends Item> targetRegistryObject;

    protected AbstractInteractableItemModelGenerator(
      final DataGenerator generator,
      final ExistingFileHelper existingFileHelper,
      final IRegistryObject<? extends Item> targetRegistryObject)
    {
        super(generator, Constants.MOD_ID, existingFileHelper);
        this.targetRegistryObject = targetRegistryObject;
    }

    @Override
    protected void registerModels()
    {
        getBuilder(
          targetRegistryObject.get().getRegistryName().getPath()
        )
          .parent(
            getExistingFile(new ResourceLocation(new ResourceLocation(Constants.MOD_ID, "item/" + Objects.requireNonNull(targetRegistryObject.get().getRegistryName()).getPath()) + "_spec"))
          )
          .customLoader((itemModelBuilder, existingFileHelper) -> new CustomLoaderBuilder<>(
            new ResourceLocation(Constants.INTERACTABLE_MODEL_LOADER),
            itemModelBuilder,
            existingFileHelper
          ) {});
    }
}
