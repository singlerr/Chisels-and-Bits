package mod.chiselsandbits.item;

import com.google.common.collect.Lists;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.measuring.IMeasuringTapeItem;
import mod.chiselsandbits.api.measuring.MeasuringMode;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.keys.KeyBindingManager;
import mod.chiselsandbits.measures.MeasuringManager;
import mod.chiselsandbits.network.packets.MeasurementsResetPacket;
import mod.chiselsandbits.network.packets.MeasurementsUpdatedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import net.minecraft.item.Item.Properties;

public class MeasuringTapeItem extends Item implements IMeasuringTapeItem
{
    public MeasuringTapeItem(final Properties properties)
    {
        super(properties);
    }

    @NotNull
    @Override
    public MeasuringMode getMode(final ItemStack stack)
    {
        if (!stack.getOrCreateTag().contains("mode"))
            return MeasuringMode.WHITE_BIT;

        return MeasuringMode.valueOf(stack.getOrCreateTag().getString("mode"));
    }

    @Override
    public void setMode(final ItemStack stack, final MeasuringMode mode)
    {
        if (mode == null)
            return;

        stack.getOrCreateTag().putString("mode", mode.toString());
    }

    @Override
    public @NotNull Collection<MeasuringMode> getPossibleModes()
    {
        return Lists.newArrayList(MeasuringMode.values());
    }

    @Override
    public ClickProcessingState handleRightClickProcessing(
      final PlayerEntity playerEntity, final Hand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        final ItemStack stack = playerEntity.getItemInHand(hand);
        if (stack.getItem() != this)
            return ClickProcessingState.DEFAULT;

        if (KeyBindingManager.getInstance().isResetMeasuringTapeKeyPressed()) {
            clear(stack);
            ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new MeasurementsResetPacket());
            return ClickProcessingState.DEFAULT;
        }

        final RayTraceResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK || !(rayTraceResult instanceof BlockRayTraceResult))
        {
            return ClickProcessingState.DEFAULT;
        }
        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
        final Vector3d hitVector = blockRayTraceResult.getLocation();

        final Optional<Vector3d> startPointHandler = getStart(stack);
        if (!startPointHandler.isPresent()) {
            setStart(stack, getMode(stack).getType().adaptPosition(hitVector));
            return new ClickProcessingState(true, Event.Result.ALLOW);
        }

        final Vector3d startPoint = startPointHandler.get();
        final Vector3d endPoint = getMode(stack).getType().adaptPosition(hitVector);

        MeasuringManager.getInstance().createAndSend(
          startPoint,
          endPoint,
          getMode(stack)
        );

        clear(stack);

        return new ClickProcessingState(true, Event.Result.ALLOW);
    }

    @Override
    public void inventoryTick(final @NotNull ItemStack stack, final @NotNull World worldIn, final @NotNull Entity entityIn, final int itemSlot, final boolean isSelected)
    {
        if (!worldIn.isClientSide())
            return;

        if (!(entityIn instanceof PlayerEntity))
            return;

        final PlayerEntity playerEntity = (PlayerEntity) entityIn;

        if (stack.getItem() != this)
            return;

        final Optional<Vector3d> startPointHandler = getStart(stack);
        if (!startPointHandler.isPresent()) {
            return;
        }

        if (KeyBindingManager.getInstance().isResetMeasuringTapeKeyPressed()) {
            clear(stack);
            ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new MeasurementsResetPacket());
            return;
        }

        final RayTraceResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK || !(rayTraceResult instanceof BlockRayTraceResult))
        {
            return;
        }
        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
        final Vector3d hitVector = blockRayTraceResult.getLocation();

        final Vector3d startPoint = startPointHandler.get();
        final Vector3d endPoint = getMode(stack).getType().adaptPosition(hitVector);

        MeasuringManager.getInstance().createAndSend(
          startPoint,
          endPoint,
          getMode(stack)
        );
    }

    @Override
    public @NotNull Optional<Vector3d> getStart(final @NotNull ItemStack stack)
    {
        if (!stack.getOrCreateTag().contains("start"))
            return Optional.empty();

        final CompoundNBT start = stack.getOrCreateTag().getCompound("start");
        return Optional.of(
          new Vector3d(
            start.getDouble("x"),
            start.getDouble("y"),
            start.getDouble("z")
          )
        );
    }

    @Override
    public void setStart(final @NotNull ItemStack stack, final @NotNull Vector3d start)
    {
        final CompoundNBT compoundNBT = new CompoundNBT();

        compoundNBT.putDouble("x", start.x());
        compoundNBT.putDouble("y", start.y());
        compoundNBT.putDouble("z", start.z());

        stack.getOrCreateTag().put("start", compoundNBT);
    }

    @Override
    public void clear(final @NotNull ItemStack stack)
    {
        stack.getOrCreateTag().remove("start");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final World worldIn, final @NotNull List<ITextComponent> tooltip, final @NotNull ITooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (KeyBindingManager.getInstance().areBindingsInitialized()) {
            Configuration.getInstance().getCommon().helpText(LocalStrings.HelpTapeMeasure, tooltip,
              Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage(),
              Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage(),
              KeyBindingManager.getInstance().getResetMeasuringTapeKeyBinding().getTranslatedKeyMessage(),
              KeyBindingManager.getInstance().getOpenToolMenuKeybinding().getTranslatedKeyMessage()
            );
        }
    }
}
