package mod.chiselsandbits.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.chiselsandbits.api.measuring.IMeasurement;
import mod.chiselsandbits.api.measuring.MeasuringMode;
import mod.chiselsandbits.api.measuring.MeasuringType;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.measures.MeasuringManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.*;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.UUID;

public final class MeasurementRenderer
{
    private static final MeasurementRenderer INSTANCE = new MeasurementRenderer();

    private MeasurementRenderer()
    {
    }

    public static MeasurementRenderer getInstance()
    {
        return INSTANCE;
    }

    public void renderMeasurements(final RenderWorldLastEvent event)
    {
        if (Minecraft.getInstance().level == null)
        {
            return;
        }

        final Collection<? extends IMeasurement> measurements = MeasuringManager.getInstance().getInWorld(Minecraft.getInstance().level);


        Vector3d vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = vector3d.x();
        double yView = vector3d.y();
        double zView = vector3d.z();

        measurements.forEach(measurement -> {
            final Vector3d startPos = measurement.getFrom();

            final AxisAlignedBB measurementBB = new AxisAlignedBB(
              Vector3d.ZERO, measurement.getSize()
            );
            final VoxelShape boundingShape = VoxelShapes.create(measurementBB);

            if (measurement.getMode().getGroup().map(g -> g != MeasuringType.DISTANCE).orElse(false))
            {
                WorldRenderer.renderShape(
                  event.getMatrixStack(),
                  Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get()),
                  boundingShape,
                  startPos.x() - xView,
                  startPos.y() - yView,
                  startPos.z() - zView,
                  (float) measurement.getMode().getColorVector().x(),
                  (float) measurement.getMode().getColorVector().y(),
                  (float) measurement.getMode().getColorVector().z(),
                  1f
                );

                final Vector3d lengths = VectorUtils.absolute(measurement.getTo().subtract(measurement.getFrom()));
                final Vector3d centerPos = measurement.getFrom().add(measurement.getTo()).multiply(0.5, 0.5, 0.5);

                if (lengths.y() > 1/16d)
                    renderMeasurementSize(event.getMatrixStack(), measurement, lengths.y(), new Vector3d(measurement.getFrom().x(), centerPos.y(), measurement.getFrom().z()));
                if (lengths.x() > 1/16d)
                    renderMeasurementSize(event.getMatrixStack(), measurement, lengths.x(), new Vector3d(centerPos.x(), measurement.getFrom().y(), measurement.getFrom().z()));
                if (lengths.z() > 1/16d)
                    renderMeasurementSize(event.getMatrixStack(), measurement, lengths.z(), new Vector3d(measurement.getFrom().x(), measurement.getFrom().y(), centerPos.z()));
            }
            else if (measurement.getMode().getGroup().map(g -> g == MeasuringType.DISTANCE).orElse(false))
            {
                final IVertexBuilder bufferIn = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get());
                bufferIn.vertex(event.getMatrixStack().last().pose(),
                  (float) (measurement.getFrom().x() - xView),
                  (float) (measurement.getFrom().y() - yView),
                  (float) (measurement.getFrom().z() - zView))
                  .color((float) measurement.getMode().getColorVector().x(),
                    (float) measurement.getMode().getColorVector().y(),
                    (float) measurement.getMode().getColorVector().z(),
                    1f).endVertex();

                bufferIn.vertex(event.getMatrixStack().last().pose(),
                  (float) (measurement.getTo().x() - xView),
                  (float) (measurement.getTo().y() - yView),
                  (float) (measurement.getTo().z() - zView))
                  .color((float) measurement.getMode().getColorVector().x(),
                    (float) measurement.getMode().getColorVector().y(),
                    (float) measurement.getMode().getColorVector().z(),
                    1f).endVertex();

                final Vector3d lengths = VectorUtils.absolute(measurement.getTo().subtract(measurement.getFrom()));
                final double totalLength = lengths.length();
                final Vector3d centerPos = measurement.getFrom().add(measurement.getTo()).multiply(0.5, 0.5, 0.5);

                if (totalLength > 1/16d)
                    renderMeasurementSize(event.getMatrixStack(), measurement, totalLength, centerPos);
            }

            Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.MEASUREMENT_LINES.get());


        });
    }
    private void renderMeasurementSize(
      final MatrixStack matrixStack,
      final IMeasurement measurement,
      final double length,
      final Vector3d position
    )
    {
        final double letterSize = 5.0;
        final double zScale = 0.001;

        final FontRenderer fontRenderer = Minecraft.getInstance().font;
        final ITextComponent size = formatLength(measurement.getMode(), length);
        final ITextComponent owner = getOwnerName(measurement.getOwner());

        final float scale = getScale(length);

        Vector3d vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = vector3d.x();
        double yView = vector3d.y();
        double zView = vector3d.z();

        matrixStack.pushPose();
        matrixStack.translate(position.x() - xView, position.y() + scale * letterSize - yView, position.z() - zView);
        performBillboardRotations(matrixStack);
        matrixStack.scale(scale, -scale, (float) zScale);
        matrixStack.translate(-fontRenderer.width(size) * 0.5, 0, 0);
        RenderSystem.disableDepthTest();
        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
        fontRenderer.drawInBatch(size.getString(), 0, 0, measurement.getMode().getColor().getColorValue(), true, matrixStack.last().pose(), buffer, true, 0, 15728880);
        matrixStack.translate(-fontRenderer.width(owner) * 0.5, -fontRenderer.lineHeight, 0);
        fontRenderer.drawInBatch(owner.getString(), 0, 0, measurement.getMode().getColor().getColorValue(), true, matrixStack.last().pose(), buffer, true, 0, 15728880);
        buffer.endBatch();
        RenderSystem.enableDepthTest();
        matrixStack.popPose();
    }

    private ITextComponent formatLength(
      final MeasuringMode mode,
      final double length
    )
    {
        final MeasuringType type = mode.getType();

        if (type == MeasuringType.DISTANCE)
        {
            return new StringTextComponent(new DecimalFormat("#.0#").format(length));
        }
        else if (type == MeasuringType.BLOCK)
        {
            return new TranslationTextComponent(Constants.MOD_ID + ".measurements.lengths.block", new DecimalFormat("#").format(Math.floor(length)));
        }

        return new TranslationTextComponent(Constants.MOD_ID + ".measurements.lengths.bit", new DecimalFormat("#").format(Math.floor(length * 16)));
    }

    private float getScale(
      final double maxLen)
    {
        final double maxFontSize = 0.04;
        final double minFontSize = 0.004;

        final double delta = Math.min(1.0, maxLen / 4.0);
        double scale = maxFontSize * delta + minFontSize * (1.0 - delta);

        if (maxLen < 0.25)
        {
            scale = minFontSize;
        }

        return (float) Math.min(maxFontSize, scale);
    }

    private void performBillboardRotations(
      final MatrixStack matrixStack)
    {
        final Entity view = Minecraft.getInstance().cameraEntity != null ? Minecraft.getInstance().cameraEntity : Minecraft.getInstance().player;
        if (view != null)
        {
            final float yaw = view.yRotO + (view.yRot - view.yRotO) * Minecraft.getInstance().getFrameTime();
            matrixStack.mulPose(new Quaternion(Vector3f.YP, 180 + -yaw, true));

            final float pitch = view.xRotO + (view.xRot - view.xRotO) * Minecraft.getInstance().getFrameTime();
            matrixStack.mulPose(new Quaternion(Vector3f.XP, -pitch, true));
        }
    }

    private ITextComponent getOwnerName(final UUID id) {
        if (id == (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : UUID.randomUUID())) {
            return new TranslationTextComponent(Constants.MOD_ID + ".measurements.owners.you");
        }

        final NetworkPlayerInfo playerInfo = Minecraft.getInstance().getConnection() != null ? Minecraft.getInstance().getConnection().getPlayerInfo(id) : null;
        if (playerInfo == null)
            return new TranslationTextComponent(Constants.MOD_ID + ".measurements.owners.unknown");

        return new TranslationTextComponent(Constants.MOD_ID + ".measurements.owners.by", playerInfo.getTabListDisplayName() != null ? this.formatPlayerDisplayName(playerInfo, playerInfo.getTabListDisplayName().copy()) : this.formatPlayerDisplayName(playerInfo, ScorePlayerTeam.formatNameForTeam(playerInfo.getTeam(), new StringTextComponent(playerInfo.getProfile().getName()))));
    }

    private ITextComponent formatPlayerDisplayName(NetworkPlayerInfo p_238524_1_, IFormattableTextComponent p_238524_2_) {
        return p_238524_1_.getGameMode() == GameType.SPECTATOR ? p_238524_2_.withStyle(TextFormatting.ITALIC) : p_238524_2_;
    }
}
