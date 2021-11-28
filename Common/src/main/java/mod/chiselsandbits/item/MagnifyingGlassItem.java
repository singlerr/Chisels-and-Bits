package mod.chiselsandbits.item;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityAnalysisResult;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.util.HelpTextUtils;
import mod.chiselsandbits.api.util.LocalStrings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MagnifyingGlassItem extends Item
{

    public MagnifyingGlassItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull Component getName(final ItemStack stack)
    {
        if (Minecraft.getInstance().level == null)
        {
            return super.getName(stack);
        }

        if (Minecraft.getInstance().hitResult == null)
        {
            return super.getName(stack);
        }

        if (Minecraft.getInstance().hitResult.getType() != HitResult.Type.BLOCK)
        {
            return super.getName(stack);
        }

        final BlockHitResult rayTraceResult = (BlockHitResult) Minecraft.getInstance().hitResult;
        final BlockState state = Minecraft.getInstance().level.getBlockState(rayTraceResult.getBlockPos());

        final IEligibilityAnalysisResult result = IEligibilityManager.getInstance().analyse(state);
        return result.isAlreadyChiseled() || result.canBeChiseled() ?
                 result.getReason().withStyle(ChatFormatting.GREEN) :
                                                                      result.getReason().withStyle(ChatFormatting.RED);
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack,
      final Level worldIn,
      @NotNull final List<Component> tooltip,
      @NotNull final TooltipFlag advanced)
    {
        super.appendHoverText(stack, worldIn, tooltip, advanced);
        HelpTextUtils.build(
          LocalStrings.HelpMagnifyingGlass, tooltip
        );
    }
}
