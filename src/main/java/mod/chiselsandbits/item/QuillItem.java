package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.item.interactable.IInteractableItem;
import mod.chiselsandbits.api.item.tool.IQuillItem;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.api.util.VectorUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import net.minecraft.item.Item.Properties;

public class QuillItem extends Item implements IQuillItem
{

    private final String CONST_INTERACTION = "Interaction";
    private final String CONST_SIMULATION = "Simulation";

    public QuillItem(final Properties properties)
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
        return 4f;
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
        return 32;
    }

    @Override
    public int getEnchantmentValue() {
        return 5;
    }

    public static void spawnParticles(Vector3d location, ItemStack polishedStack, World world) {
        for (int i = 0; i < 20; i++) {
            Vector3d motion = VectorUtils.offsetRandomly(Vector3d.ZERO, world.random, 1 / 8f);
            world.addParticle(new ItemParticleData(ParticleTypes.ITEM, polishedStack), location.x, location.y,
              location.z, motion.x, motion.y, motion.z);
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof PlayerEntity))
            return;
        PlayerEntity player = (PlayerEntity) entityLiving;
        if (isInteracting(stack)) {
            ItemStack interactionTarget = getInteractionTarget(stack);
            player.inventory.placeItemBackInInventory(worldIn, interactionTarget);
            stack.getOrCreateTag().remove(CONST_INTERACTION);
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull LivingEntity entityLiving) {
        if (!(entityLiving instanceof PlayerEntity))
            return stack;
        PlayerEntity player = (PlayerEntity) entityLiving;
        if (isInteracting(stack)) {
            ItemStack target = getInteractionTarget(stack);
            ItemStack pattern = createPattern(player);

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
                    player.inventory.placeItemBackInInventory(worldIn, pattern);
                }
            }
            stack.getOrCreateTag().remove(CONST_INTERACTION);
            stack.hurtAndBreak(1, entityLiving, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
        }

        return stack;
    }

    @Override
    public @NotNull ActionResult<ItemStack> use(@NotNull World worldIn, PlayerEntity playerIn, @NotNull Hand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        ActionResult<ItemStack> FAIL = new ActionResult<>(ActionResultType.FAIL, itemstack);

        if (isInteracting(itemstack)) {
            playerIn.startUsingItem(handIn);
            return new ActionResult<>(ActionResultType.PASS, itemstack);
        }

        Hand otherHand = handIn == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack itemInOtherHand = playerIn.getItemInHand(otherHand);
        if (createPattern(playerIn).getItem() != Items.PAPER && itemInOtherHand.getItem() == Items.PAPER) {
            ItemStack item = itemInOtherHand.copy();
            ItemStack target = item.split(1);
            playerIn.startUsingItem(handIn);
            itemstack.getOrCreateTag()
              .put(CONST_INTERACTION, target.serializeNBT());
            playerIn.setItemInHand(otherHand, item);
            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }

    private static ItemStack createPattern(final PlayerEntity playerEntity) {
        final RayTraceResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK || !(rayTraceResult instanceof BlockRayTraceResult))
        {
            return new ItemStack(Items.PAPER);
        }

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
        final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().in(playerEntity.getCommandSenderWorld(), blockRayTraceResult.getBlockPos());
        return areaMutator.createSnapshot().toItemStack().toPatternStack();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final World worldIn, final @NotNull List<ITextComponent> tooltip, final @NotNull ITooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpQuill, tooltip);
    }
}
