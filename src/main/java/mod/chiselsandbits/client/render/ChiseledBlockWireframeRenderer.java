package mod.chiselsandbits.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.world.IWorldAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.client.model.baked.chiseled.ChiselRenderType;
import mod.chiselsandbits.client.model.baked.chiseled.ChiseledBlockBakedModel;
import mod.chiselsandbits.client.model.baked.chiseled.ChiseledBlockBakedModelManager;
import mod.chiselsandbits.client.model.baked.simple.CombinedModel;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
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

    public void renderShape(final MatrixStack stack, final IAreaAccessor accessor, final BlockState primaryState, final BlockPos position) {
        stack.pushPose();

        final IBakedModel bakedModel = buildAccessorModel(accessor, primaryState);
        final VoxelShape wireFrame = VoxelShapeManager.getInstance().get(
          accessor,
          accessor1 -> NONE_AIR_PREDICATE
        );

        Vector3d vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = vector3d.x();
        double yView = vector3d.y();
        double zView = vector3d.z();

        stack.pushPose();
        stack.translate(position.getX() - xView, position.getY() - yView, position.getZ() - zView);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer()
          .renderModel(stack.last(),
            Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.WIREFRAME_BODY.get()),
            primaryState,
            bakedModel,
            48/255f, 120/255f, 201/255f, //Block Color does not matter
            15728880, //Full brightness
            OverlayTexture.NO_OVERLAY,
            EmptyModelData.INSTANCE);
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.WIREFRAME_BODY.get());

        stack.popPose();

        RenderSystem.disableDepthTest();
        WorldRenderer.renderShape(
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

    private IBakedModel buildAccessorModel(final IAreaAccessor accessor, final BlockState primaryState) {
        //TODO: Possibly cache the result here.

        return new CombinedModel(
          Arrays.stream(ChiselRenderType.values())
            .map(renderType -> ChiseledBlockBakedModelManager.getInstance().get(accessor, primaryState, renderType))
            .toArray(IBakedModel[]::new)
        );
    }
}
