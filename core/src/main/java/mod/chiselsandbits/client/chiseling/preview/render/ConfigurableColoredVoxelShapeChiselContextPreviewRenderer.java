package mod.chiselsandbits.client.chiseling.preview.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.axissize.CollisionType;
import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.client.render.preview.chiseling.IChiselContextPreviewRenderer;
import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.render.ModRenderTypes;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ConfigurableColoredVoxelShapeChiselContextPreviewRenderer implements IChiselContextPreviewRenderer
{
    static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "default");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void renderExistingContextsBoundingBox(
      final PoseStack poseStack, final IChiselingContext currentContextSnapshot)
    {
        if (currentContextSnapshot.getMutator().isEmpty())
            return;

        Vec3 Vec3 = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = Vec3.x();
        double yView = Vec3.y();
        double zView = Vec3.z();

        final VoxelShape renderedShape;
        final BlockPos inWorldStartPos = new BlockPos(currentContextSnapshot.getMutator().get().getInWorldStartPoint());
        final VoxelShape modeShape = currentContextSnapshot.getMode().getShape(currentContextSnapshot);
        LoggerFactory.getLogger(ConfigurableColoredVoxelShapeChiselContextPreviewRenderer.class).info("modeShape: " + modeShape);
        VoxelShape boundingShape = null;

        if (currentContextSnapshot.getMode().requiresRestrainingOfShape() || IClientConfiguration.getInstance().getMutatorPreviewDebug().get()) {
            boundingShape = VoxelShapeManager.getInstance()
                    .get(currentContextSnapshot.getMutator().get(),
                            !currentContextSnapshot.getDisplayedModeOfOperandus().processesAir() ? CollisionType.NONE_AIR : CollisionType.ALL,
                            false);
        }

        if (currentContextSnapshot.getMode().requiresRestrainingOfShape()) {
            renderedShape = Shapes.joinUnoptimized(boundingShape, modeShape, BooleanOp.AND);
        }
        else {
            renderedShape = modeShape;
        }



        final List<? extends Float> color = getColor(currentContextSnapshot);
        final List<? extends Float> mutatorColor = getMutatorColor(currentContextSnapshot);

        LevelRenderer.renderShape(
          poseStack,
          Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.CHISEL_PREVIEW_INSIDE_BLOCKS.get()),
          renderedShape,
          inWorldStartPos.getX() - xView, inWorldStartPos.getY() - yView, inWorldStartPos.getZ() - zView,
          getColorValue(color, 0, 0f) * 0.3f,
          getColorValue(color, 1, 0f) * 0.3f,
          getColorValue(color, 2, 0f) * 0.3f,
          getColorValue(color, 3, 1f) * 0.3f
        );
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.CHISEL_PREVIEW_INSIDE_BLOCKS.get());

        LevelRenderer.renderShape(
                poseStack,
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.CHISEL_PREVIEW_OUTSIDE_BLOCKS.get()),
                renderedShape,
                inWorldStartPos.getX() - xView, inWorldStartPos.getY() - yView, inWorldStartPos.getZ() - zView,
                getColorValue(color, 0, 0f),
                getColorValue(color, 1, 0f),
                getColorValue(color, 2, 0f),
                getColorValue(color, 3, 1f)
        );
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.CHISEL_PREVIEW_OUTSIDE_BLOCKS.get());

        if (IClientConfiguration.getInstance().getMutatorPreviewDebug().get()) {
            LevelRenderer.renderShape(
              poseStack,
              Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get()),
              boundingShape,
              inWorldStartPos.getX() - xView, inWorldStartPos.getY() - yView, inWorldStartPos.getZ() - zView,
              getColorValue(mutatorColor, 0, 0f),
              getColorValue(mutatorColor, 1, 0f),
              getColorValue(mutatorColor, 2, 0f),
              getColorValue(mutatorColor, 3, 1f)
            );
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.MEASUREMENT_LINES.get());
        }
    }

    private static List<? extends Float> getMutatorColor(IChiselingContext currentContextSnapshot) {
        return switch (currentContextSnapshot.getDisplayedModeOfOperandus()) {
            case CHISELING -> IClientConfiguration.getInstance().getMutatorPreviewChiselingColor().get();
            case PLACING -> IClientConfiguration.getInstance().getMutatorPreviewPlacementColor().get();
            case ALTERATION -> IClientConfiguration.getInstance().getMutatorPreviewAlterationColor().get();
        };
    }

    private static List<? extends Float> getColor(IChiselingContext currentContextSnapshot) {
        return switch (currentContextSnapshot.getDisplayedModeOfOperandus()) {
            case CHISELING -> IClientConfiguration.getInstance().getPreviewChiselingColor().get();
            case PLACING -> IClientConfiguration.getInstance().getPreviewPlacementColor().get();
            case ALTERATION -> IClientConfiguration.getInstance().getPreviewAlterationColor().get();
        };
    }

    private static float getColorValue(final List<? extends Float> values, final int index, final float defaultValue) {
        if (values.size() <= index || index < 0)
            return defaultValue;

        final Number value = values.get(index);
        if (0 <= value.floatValue() && value.floatValue() <= 1f)
            return value.floatValue();

        return defaultValue;
    }
}
