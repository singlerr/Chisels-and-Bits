package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SealingNotSupportedException;
import mod.chiselsandbits.api.item.tool.ISealantItem;
import mod.chiselsandbits.api.sealing.ISupportsSealing;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.VectorUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        return isInteracting(stack) ? ItemStack.read(stack.getOrCreateChildTag(CONST_INTERACTION)) : ItemStack.EMPTY;
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
    public int getItemEnchantability() {
        return 5;
    }

    public static void spawnParticles(Vector3d location, ItemStack polishedStack, World world) {
        for (int i = 0; i < 20; i++) {
            Vector3d motion = VectorUtils.offsetRandomly(Vector3d.ZERO, world.rand, 1 / 8f);
            world.addParticle(new ItemParticleData(ParticleTypes.ITEM, polishedStack), location.x, location.y,
              location.z, motion.x, motion.y, motion.z);
        }
    }

    @Override
    public void onPlayerStoppedUsing(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull LivingEntity entityLiving, int timeLeft) {
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
    public @NotNull ItemStack onItemUseFinish(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull LivingEntity entityLiving) {
        if (!(entityLiving instanceof PlayerEntity))
            return stack;
        PlayerEntity player = (PlayerEntity) entityLiving;
        if (isInteracting(stack)) {
            ItemStack target = getInteractionTarget(stack);
            ItemStack pattern = createPattern(target);

            if (worldIn.isRemote) {
                spawnParticles(entityLiving.getEyePosition(1)
                                 .add(entityLiving.getLookVec()
                                        .scale(.5f)),
                  target, worldIn);
                return stack;
            }

            if (!pattern.isEmpty()) {
                if (player instanceof FakePlayer) {
                    player.dropItem(pattern, false, false);
                } else {
                    player.inventory.placeItemBackInInventory(worldIn, pattern);
                }
            }
            stack.getOrCreateTag().remove(CONST_INTERACTION);
            stack.damageItem(1, entityLiving, p -> p.sendBreakAnimation(p.getActiveHand()));
        }

        return stack;
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World worldIn, PlayerEntity playerIn, @NotNull Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        ActionResult<ItemStack> FAIL = new ActionResult<>(ActionResultType.FAIL, itemstack);

        if (isInteracting(itemstack)) {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(ActionResultType.PASS, itemstack);
        }

        Hand otherHand = handIn == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack itemInOtherHand = playerIn.getHeldItem(otherHand);
        if (createPattern(itemInOtherHand).getItem() != itemInOtherHand.getItem() && itemInOtherHand.getItem() instanceof ISupportsSealing) {
            ItemStack item = itemInOtherHand.copy();
            ItemStack target = item.split(1);
            playerIn.setActiveHand(handIn);
            itemstack.getOrCreateTag()
              .put(CONST_INTERACTION, target.serializeNBT());
            playerIn.setHeldItem(otherHand, item);
            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
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
    public void addInformation(
      final @NotNull ItemStack stack, @Nullable final World worldIn, final @NotNull List<ITextComponent> tooltip, final @NotNull ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpSealant, tooltip);
    }
}
