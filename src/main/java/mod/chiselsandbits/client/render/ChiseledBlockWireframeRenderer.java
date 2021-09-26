package mod.chiselsandbits.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.client.model.baked.chiseled.ChiselRenderType;
import mod.chiselsandbits.client.model.baked.chiseled.ChiseledBlockBakedModelManager;
import mod.chiselsandbits.client.model.baked.simple.CombinedModel;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Arrays;
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

    public void renderShape(final PoseStack stack, final IAreaAccessor accessor, final BlockState primaryState, final BlockPos position) {
        stack.pushPose();

        final VoxelShape wireFrame = VoxelShapeManager.getInstance().get(
          accessor,
          accessor1 -> NONE_AIR_PREDICATE
        );

        Vec3 vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = vector3d.x();
        double yView = vector3d.y();
        double zView = vector3d.z();

        RenderSystem.disableDepthTest();
        LevelRenderer.renderShape(
          stack,
          Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.WIREFRAME_LINES.get()),
          wireFrame,
          position.getX() - xView, position.getY() - yView, position.getZ() - zView,
          1f, 1f, 1f, 1f
        );
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.WIREFRAME_LINES.get());
        RenderSystem.enableDepthTest();

        stack.popPose();
    }

    private BakedModel buildAccessorModel(final IAreaAccessor accessor, final BlockState primaryState) {
        //TODO: Possibly cache the result here.

        return new CombinedModel(
          Arrays.stream(ChiselRenderType.values())
            .map(renderType -> ChiseledBlockBakedModelManager.getInstance().get(accessor, primaryState, renderType))
            .toArray(BakedModel[]::new)
        );
    }
}
