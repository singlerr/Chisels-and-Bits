package mod.chiselsandbits.forge.integration.chiselsandbits.create;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.client.model.data.ChiseledBlockModelDataManager;
import net.minecraft.client.Minecraft;
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
      final MaterialManager materialManager, final PlacementSimulationWorld simulationWorld, final MovementContext context)
    {
        if (context.temporaryData == null) {
            final ChiseledBlockEntity chiseledBlockEntity = new ChiseledBlockEntity(
              context.localPos.offset(context.contraption.anchor),
              context.state
            );
            chiseledBlockEntity.setLevel(context.world);

            final CreateModelUpdateHolder modelUpdateHolder = new CreateModelUpdateHolder();
            context.temporaryData = modelUpdateHolder;

            chiseledBlockEntity.load(context.tileData);
            ChiseledBlockModelDataManager.getInstance().updateModelData(
              chiseledBlockEntity,
              () -> Minecraft.getInstance().execute(() -> modelUpdateHolder.setModelData(chiseledBlockEntity.getBlockModelData())),
              true
            );
        }

        return new ChiseledBlockActorInstance(materialManager, simulationWorld, context, (CreateModelUpdateHolder) context.temporaryData);
    }
}
