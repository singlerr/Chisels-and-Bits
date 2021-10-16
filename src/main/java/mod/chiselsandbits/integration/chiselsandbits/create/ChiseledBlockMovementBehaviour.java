package mod.chiselsandbits.integration.chiselsandbits.create;

import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.client.model.data.ChiseledBlockModelDataManager;
import org.jetbrains.annotations.Nullable;

public class ChiseledBlockMovementBehaviour extends MovementBehaviour
{
    @Override
    public boolean hasSpecialInstancedRendering()
    {
        return true;
    }

    @Nullable
    @Override
    public ActorInstance createInstance(
      final MaterialManager<?> materialManager, final PlacementSimulationWorld simulationWorld, final MovementContext context)
    {
        if (context.temporaryData == null) {
            final ChiseledBlockEntity chiseledBlockEntity = new ChiseledBlockEntity();
            chiseledBlockEntity.setLevelAndPosition(context.world, context.localPos.offset(context.contraption.anchor));

            final CreateModelUpdateHolder modelUpdateHolder = new CreateModelUpdateHolder();
            context.temporaryData = modelUpdateHolder;

            if (!chiseledBlockEntity.deserializeOrUpdateNBTData(context.tileData)) {
                ChiseledBlockModelDataManager.getInstance().updateModelData(
                  chiseledBlockEntity,
                  () -> modelUpdateHolder.setModelData(chiseledBlockEntity.getModelData()),
                  true
                );
            }
        }

        return new ChiseledBlockActorInstance(materialManager, simulationWorld, context, (CreateModelUpdateHolder) context.temporaryData);
    }
}
