package mod.chiselsandbits.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

import java.util.function.Predicate;

public class ChiseledBlockWireframeRenderer
{
    private static final ChiseledBlockWireframeRenderer INSTANCE = new ChiseledBlockWireframeRenderer();
    private static final Predicate<IStateEntryInfo> NONE_AIR_PREDICATE = new Predicate<IStateEntryInfo>() {
        @Override
        public boolean test(final IStateEntryInfo iStateEntryInfo)
        {
            return !iStateEntryInfo.getState().isAir();
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

    public void renderShape(final MatrixStack stack, final VoxelShape wireFrame, final Vector3d position, final Vector3d color) {
        stack.pushPose();

        Vector3d vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = vector3d.x();
        double yView = vector3d.y();
        double zView = vector3d.z();

        //48/255f, 120/255f, 201/255f
        RenderSystem.disableDepthTest();
        WorldRenderer.renderShape(
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
