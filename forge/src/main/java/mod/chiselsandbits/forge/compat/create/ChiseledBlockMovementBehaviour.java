package mod.chiselsandbits.forge.compat.create;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import org.jetbrains.annotations.Nullable;

public class ChiseledBlockMovementBehaviour implements MovementBehaviour {

    @Override
    public boolean hasSpecialInstancedRendering() {
        return true;
    }

    @Nullable
    @Override
    public ActorInstance createInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld,
                                        MovementContext context) {
        return new ChiseledBlockActorInstance(materialManager, simulationWorld, context);
    }
}
