package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SealingNotSupportedException;
import mod.chiselsandbits.api.item.tool.ISealantItem;
import mod.chiselsandbits.api.sealing.ISupportsSealing;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.client.ister.InteractionISTER;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class SealantItem extends Item implements ISealantItem
{

    private final String CONST_INTERACTION = "Interaction";
    private final String CONST_SIMULATION = "Simulation";

    public SealantItem(final Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean isInteracting(final ItemStack stack)
    {
        return stack.getItem() == this && stack.getOrCreateTag().contains(CONST_INTERACTION);
    }

    @Override
    public ItemStack getInteractionTarget(final ItemStack stack)
    {
        return isInteracting(stack) ? ItemStack.of(stack.getOrCreateTagElement(CONST_INTERACTION)) : ItemStack.EMPTY;
    }

    @Override
    public boolean isRunningASimulatedInteraction(final ItemStack stack)
    {
        return isInteracting(stack) && stack.getOrCreateTag().contains(CONST_SIMULATION);
    }

    @Override
    public float getBobbingTickCount()
    {
        return 32;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return 5;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 64;
    }

    @Override
    public int getEnchantmentValue() {
        return 5;
    }

    public static void spawnParticles(Vec3 location, ItemStack polishedStack, Level world) {
        for (int i = 0; i < 20; i++) {
            Vec3 motion = VectorUtils.offsetRandomly(Vec3.ZERO, world.random, 1 / 8f);
            world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, polishedStack), location.x, location.y,
              location.z, motion.x, motion.y, motion.z);
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player))
            return;
        if (isInteracting(stack)) {
            ItemStack interactionTarget = getInteractionTarget(stack);
            player.getInventory().placeItemBackInInventory(interactionTarget);
            stack.getOrCreateTag().remove(CONST_INTERACTION);
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity entityLiving) {
        if (!(entityLiving instanceof Player player))
            return stack;
        if (isInteracting(stack)) {
            ItemStack target = getInteractionTarget(stack);
            ItemStack pattern = createPattern(target);

            if (worldIn.isClientSide) {
                spawnParticles(entityLiving.getEyePosition(1)
                                 .add(entityLiving.getLookAngle()
                                        .scale(.5f)),
                  target, worldIn);
                return stack;
            }

            if (!pattern.isEmpty()) {
                if (player instanceof FakePlayer) {
                    player.drop(pattern, false, false);
                } else {
                    player.getInventory().placeItemBackInInventory(pattern);
                }
            }
            stack.getOrCreateTag().remove(CONST_INTERACTION);
            stack.hurtAndBreak(1, entityLiving, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
        }

        return stack;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);

        if (isInteracting(itemstack)) {
            playerIn.startUsingItem(handIn);
            return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
        }

        InteractionHand otherHand = handIn == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack itemInOtherHand = playerIn.getItemInHand(otherHand);
        if (createPattern(itemInOtherHand).getItem() != itemInOtherHand.getItem() && itemInOtherHand.getItem() instanceof ISupportsSealing) {
            ItemStack item = itemInOtherHand.copy();
            ItemStack target = item.split(1);
            playerIn.startUsingItem(handIn);
            itemstack.getOrCreateTag()
              .put(CONST_INTERACTION, target.serializeNBT());
            playerIn.setItemInHand(otherHand, item);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }

    private static ItemStack createPattern(final ItemStack targetStack) {
        if (targetStack.getItem() instanceof ISupportsSealing) {
            try
            {
                return ((ISupportsSealing) targetStack.getItem()).seal(targetStack);
            }
            catch (SealingNotSupportedException e)
            {
                return targetStack;
            }
        }

        return targetStack;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final Level worldIn, final @NotNull List<Component> tooltip, final @NotNull TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpSealant, tooltip);
    }

    @Override
    public void initializeClient(final Consumer<IItemRenderProperties> consumer)
    {
        consumer.accept(new IItemRenderProperties() {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer()
            {
                return new InteractionISTER();
            }
        });
    }
}
