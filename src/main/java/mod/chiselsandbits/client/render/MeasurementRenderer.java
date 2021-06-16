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
        if (Minecraft.getInstance().world == null)
        {
            return;
        }

        final Collection<? extends IMeasurement> measurements = MeasuringManager.getInstance().getInWorld(Minecraft.getInstance().world);


        Vector3d vector3d = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        double xView = vector3d.getX();
        double yView = vector3d.getY();
        double zView = vector3d.getZ();

        measurements.forEach(measurement -> {
            final Vector3d startPos = measurement.getFrom();

            final AxisAlignedBB measurementBB = new AxisAlignedBB(
              Vector3d.ZERO, measurement.getSize()
            );
            final VoxelShape boundingShape = VoxelShapes.create(measurementBB);

            if (measurement.getMode().getGroup().map(g -> g != MeasuringType.DISTANCE).orElse(false))
            {
                WorldRenderer.drawShape(
                  event.getMatrixStack(),
                  Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get()),
                  boundingShape,
                  startPos.getX() - xView,
                  startPos.getY() - yView,
                  startPos.getZ() - zView,
                  (float) measurement.getMode().getColorVector().getX(),
                  (float) measurement.getMode().getColorVector().getY(),
                  (float) measurement.getMode().getColorVector().getZ(),
                  1f
                );

                final Vector3d lengths = VectorUtils.absolute(measurement.getTo().subtract(measurement.getFrom()));
                final Vector3d centerPos = measurement.getFrom().add(measurement.getTo()).mul(0.5, 0.5, 0.5);

                if (lengths.getY() > 1/16d)
                    renderMeasurementSize(event.getMatrixStack(), measurement, lengths.getY(), new Vector3d(measurement.getFrom().getX(), centerPos.getY(), measurement.getFrom().getZ()));
                if (lengths.getX() > 1/16d)
                    renderMeasurementSize(event.getMatrixStack(), measurement, lengths.getX(), new Vector3d(centerPos.getX(), measurement.getFrom().getY(), measurement.getFrom().getZ()));
                if (lengths.getZ() > 1/16d)
                    renderMeasurementSize(event.getMatrixStack(), measurement, lengths.getZ(), new Vector3d(measurement.getFrom().getX(), measurement.getFrom().getY(), centerPos.getZ()));
            }
            else if (measurement.getMode().getGroup().map(g -> g == MeasuringType.DISTANCE).orElse(false))
            {
                final IVertexBuilder bufferIn = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get());
                bufferIn.pos(event.getMatrixStack().getLast().getMatrix(),
                  (float) (measurement.getFrom().getX() - xView),
                  (float) (measurement.getFrom().getY() - yView),
                  (float) (measurement.getFrom().getZ() - zView))
                  .color((float) measurement.getMode().getColorVector().getX(),
                    (float) measurement.getMode().getColorVector().getY(),
                    (float) measurement.getMode().getColorVector().getZ(),
                    1f).endVertex();

                bufferIn.pos(event.getMatrixStack().getLast().getMatrix(),
                  (float) (measurement.getTo().getX() - xView),
                  (float) (measurement.getTo().getY() - yView),
                  (float) (measurement.getTo().getZ() - zView))
                  .color((float) measurement.getMode().getColorVector().getX(),
                    (float) measurement.getMode().getColorVector().getY(),
                    (float) measurement.getMode().getColorVector().getZ(),
                    1f).endVertex();

                final Vector3d lengths = VectorUtils.absolute(measurement.getTo().subtract(measurement.getFrom()));
                final double totalLength = lengths.length();
                final Vector3d centerPos = measurement.getFrom().add(measurement.getTo()).mul(0.5, 0.5, 0.5);

                if (totalLength > 1/16d)
                    renderMeasurementSize(event.getMatrixStack(), measurement, totalLength, centerPos);
            }

            Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(ModRenderTypes.MEASUREMENT_LINES.get());


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

        final FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        final ITextComponent size = formatLength(measurement.getMode(), length);
        final ITextComponent owner = getOwnerName(measurement.getOwner());

        final float scale = getScale(length);

        Vector3d vector3d = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        double xView = vector3d.getX();
        double yView = vector3d.getY();
        double zView = vector3d.getZ();

        matrixStack.push();
        matrixStack.translate(position.getX() - xView, position.getY() + scale * letterSize - yView, position.getZ() - zView);
        performBillboardRotations(matrixStack);
        matrixStack.scale(scale, -scale, (float) zScale);
        matrixStack.translate(-fontRenderer.getStringPropertyWidth(size) * 0.5, 0, 0);
        RenderSystem.disableDepthTest();
        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        fontRenderer.renderString(size.getString(), 0, 0, measurement.getMode().getColor().getColorValue(), true, matrixStack.getLast().getMatrix(), buffer, true, 0, 15728880);
        matrixStack.translate(-fontRenderer.getStringPropertyWidth(owner) * 0.5, -fontRenderer.FONT_HEIGHT, 0);
        fontRenderer.renderString(owner.getString(), 0, 0, measurement.getMode().getColor().getColorValue(), true, matrixStack.getLast().getMatrix(), buffer, true, 0, 15728880);
        buffer.finish();
        RenderSystem.enableDepthTest();
        matrixStack.pop();
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
        final Entity view = Minecraft.getInstance().renderViewEntity != null ? Minecraft.getInstance().renderViewEntity : Minecraft.getInstance().player;
        if (view != null)
        {
            final float yaw = view.prevRotationYaw + (view.rotationYaw - view.prevRotationYaw) * Minecraft.getInstance().getRenderPartialTicks();
            matrixStack.rotate(new Quaternion(Vector3f.YP, 180 + -yaw, true));

            final float pitch = view.prevRotationPitch + (view.rotationPitch - view.prevRotationPitch) * Minecraft.getInstance().getRenderPartialTicks();
            matrixStack.rotate(new Quaternion(Vector3f.XP, -pitch, true));
        }
    }

    private ITextComponent getOwnerName(final UUID id) {
        if (id == (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUniqueID() : UUID.randomUUID())) {
            return new TranslationTextComponent(Constants.MOD_ID + ".measurements.owners.you");
        }

        final NetworkPlayerInfo playerInfo = Minecraft.getInstance().getConnection() != null ? Minecraft.getInstance().getConnection().getPlayerInfo(id) : null;
        if (playerInfo == null)
            return new TranslationTextComponent(Constants.MOD_ID + ".measurements.owners.unknown");

        return new TranslationTextComponent(Constants.MOD_ID + ".measurements.owners.by", playerInfo.getDisplayName() != null ? this.formatPlayerDisplayName(playerInfo, playerInfo.getDisplayName().deepCopy()) : this.formatPlayerDisplayName(playerInfo, ScorePlayerTeam.func_237500_a_(playerInfo.getPlayerTeam(), new StringTextComponent(playerInfo.getGameProfile().getName()))));
    }

    private ITextComponent formatPlayerDisplayName(NetworkPlayerInfo p_238524_1_, IFormattableTextComponent p_238524_2_) {
        return p_238524_1_.getGameType() == GameType.SPECTATOR ? p_238524_2_.mergeStyle(TextFormatting.ITALIC) : p_238524_2_;
    }
}
