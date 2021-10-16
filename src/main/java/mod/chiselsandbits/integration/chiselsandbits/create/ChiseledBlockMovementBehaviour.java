package mod.chiselsandbits.integration.chiselsandbits.create;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class ChiseledBlockMovementBehaviour extends MovementBehaviour
{
    @Override
    public void renderInContraption(
      final MovementContext context, final PlacementSimulationWorld renderWorld, final ContraptionMatrices matrices, final IRenderTypeBuffer buffer)
    {
        super.renderInContraption(context, renderWorld, matrices, buffer);

    }
}
