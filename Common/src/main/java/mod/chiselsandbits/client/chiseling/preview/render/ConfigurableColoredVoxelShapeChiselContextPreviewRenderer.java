package mod.chiselsandbits.client.chiseling.preview.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.client.chiseling.preview.render.IChiselContextPreviewRenderer;
import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.render.ModRenderTypes;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static mod.chiselsandbits.api.util.StateEntryPredicates.ALL;
import static mod.chiselsandbits.api.util.StateEntryPredicates.NOT_AIR;

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

        final BlockPos inWorldStartPos = new BlockPos(currentContextSnapshot.getMutator().get().getInWorldStartPoint());
        final VoxelShape boundingShape = VoxelShapeManager.getInstance()
          .get(currentContextSnapshot.getMutator().get(),
            areaAccessor -> {
                final Predicate<IStateEntryInfo> contextPredicate = currentContextSnapshot.getStateFilter()
                  .map(factory -> factory.apply(areaAccessor))
                  .orElse(currentContextSnapshot.getModeOfOperandus() == ChiselingOperation.CHISELING ? NOT_AIR : ALL);

                return new InternalContextFilter(contextPredicate);
            },
            false);

        final List<? extends Float> color = currentContextSnapshot.getModeOfOperandus() == ChiselingOperation.CHISELING ?
                                 IClientConfiguration.getInstance().getPreviewChiselingColor().get() :
                                 IClientConfiguration.getInstance().getPreviewPlacementColor().get();

        RenderSystem.disableDepthTest();
        LevelRenderer.renderShape(
          poseStack,
          Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get()),
          boundingShape,
          inWorldStartPos.getX() - xView, inWorldStartPos.getY() - yView, inWorldStartPos.getZ() - zView,
          getColorValue(color, 0, 0f),
          getColorValue(color, 1, 0f),
          getColorValue(color, 2, 0f),
          getColorValue(color, 3, 1f)
        );
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.MEASUREMENT_LINES.get());
        RenderSystem.enableDepthTest();
    }

    private static float getColorValue(final List<? extends Float> values, final int index, final float defaultValue) {
        if (values.size() <= index || index < 0)
            return defaultValue;

        final Number value = values.get(index);
        if (0 <= value.floatValue() && value.floatValue() <= 1f)
            return value.floatValue();

        return defaultValue;
    }

    private static final class InternalContextFilter implements Predicate<IStateEntryInfo>
    {
        private final Predicate<IStateEntryInfo> placingContextPredicate;

        private InternalContextFilter(final Predicate<IStateEntryInfo> placingContextPredicate) {this.placingContextPredicate = placingContextPredicate;}

        @Override
        public boolean test(final IStateEntryInfo s)
        {
            return (s.getState().isAir() || IEligibilityManager.getInstance().canBeChiseled(s.getState())) && placingContextPredicate.test(s);
        }

        @Override
        public int hashCode()
        {
            return placingContextPredicate != null ? placingContextPredicate.hashCode() : 0;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof InternalContextFilter))
            {
                return false;
            }

            final InternalContextFilter that = (InternalContextFilter) o;

            return Objects.equals(placingContextPredicate, that.placingContextPredicate);
        }
    }
}
