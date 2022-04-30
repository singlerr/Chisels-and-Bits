package mod.chiselsandbits.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Predicate;

public class ChiseledBlockWireframeRenderer
{
    private static final ChiseledBlockWireframeRenderer INSTANCE = new ChiseledBlockWireframeRenderer();
    private static final Predicate<IStateEntryInfo> NONE_AIR_PREDICATE = new Predicate<>()
    {
        @Override
        public boolean test(final IStateEntryInfo iStateEntryInfo)
        {
            return !iStateEntryInfo.getBlockInformation().isAir();
        }

        @Override
        public int hashCode()
        {
            return 2;
        }

        @Override
        public boolean equals(final Object obj)
        {
            return this == obj;
        }
    };

    public static ChiseledBlockWireframeRenderer getInstance()
    {
        return INSTANCE;
    }

    private ChiseledBlockWireframeRenderer()
    {
    }

    public void renderShape(final PoseStack stack, final VoxelShape wireFrame, final Vec3 position, final Vec3 color) {
        stack.pushPose();

        Vec3 vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = vector3d.x();
        double yView = vector3d.y();
        double zView = vector3d.z();

        //48/255f, 120/255f, 201/255f
        RenderSystem.disableDepthTest();
        LevelRenderer.renderShape(
          stack,
          Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.WIREFRAME_LINES.get()),
          wireFrame,
          position.x() - xView, position.y() - yView, position.z() - zView,
          (float) color.x(), (float) color.y(), (float) color.z() , 1f
        );
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.WIREFRAME_LINES.get());
        RenderSystem.enableDepthTest();

        stack.popPose();
    }
}
