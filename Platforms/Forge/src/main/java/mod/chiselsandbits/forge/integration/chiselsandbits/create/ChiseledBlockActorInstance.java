package mod.chiselsandbits.forge.integration.chiselsandbits.create;

import com.google.common.collect.Sets;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModModelProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.material.Material;

import java.util.Set;
import java.util.UUID;

public class ChiseledBlockActorInstance extends ActorInstance
{
    private final Set<ModelData> modelDataSet = Sets.newConcurrentHashSet();

    public ChiseledBlockActorInstance(
      final MaterialManager materialManager,
      final PlacementSimulationWorld world,
      final MovementContext context,
      final CreateModelUpdateHolder modelUpdateHolder)
    {
        super(materialManager, world, context);

        modelUpdateHolder.addConsumer(modelData -> {
            if (!modelData.hasProperty(ModModelProperties.KNOWN_LAYER_MODEL_PROPERTY) && !modelData.hasProperty(ModModelProperties.UNKNOWN_LAYER_MODEL_PROPERTY))
            {
                return;
            }

            this.modelDataSet.forEach(InstanceData::delete);
            this.modelDataSet.clear();

            if (!modelData.hasProperty(ModModelProperties.KNOWN_LAYER_MODEL_PROPERTY))
            {
                final BakedModel model = modelData.getData(ModModelProperties.UNKNOWN_LAYER_MODEL_PROPERTY);
                final LightAwareBlockModel blockModel = new LightAwareBlockModel(
                  model, ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.get(Material.STONE).get().defaultBlockState());

                if (!blockModel.empty())
                {
                    this.modelDataSet.add(
                      materialManager
                        .defaultTransparent()
                        .material(CreateMaterials.TRANSFORMED)
                        .model(UUID.randomUUID(), () -> blockModel)
                        .createInstance()
                    );
                }

                return;
            }

            modelData.getData(ModModelProperties.KNOWN_LAYER_MODEL_PROPERTY)
              .forEach((type, model) -> {
                  final LightAwareBlockModel blockModel = new LightAwareBlockModel(model,
                    ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.get(Material.STONE).get().defaultBlockState());

                  if (!blockModel.empty())
                  {
                      this.modelDataSet.add(
                        materialManager
                          .defaultTransparent()
                          .material(CreateMaterials.TRANSFORMED)
                          .model(UUID.randomUUID(), () -> blockModel)
                          .createInstance()
                      );
                  }
              });
        });
    }

    @Override
    public void beginFrame()
    {
        PoseStack ms = new PoseStack();
        MatrixTransformStack msr = new MatrixTransformStack(ms);

        msr.translate(context.localPos);

        this.modelDataSet.forEach(modelData -> modelData.setTransform(ms));
    }
}
