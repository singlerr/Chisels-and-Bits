package mod.chiselsandbits.items;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class ItemMagnifyingGlass extends Item
{

	public ItemMagnifyingGlass(Properties properties)
	{
        super(properties.maxStackSize(1));
	}

	@OnlyIn(Dist.CLIENT)
    @Override
    public ITextComponent getHighlightTip(final ItemStack item, final ITextComponent displayName)
    {
        if (Minecraft.getInstance().objectMouseOver == null)
            return displayName;

        if (Minecraft.getInstance().objectMouseOver.getType() != RayTraceResult.Type.BLOCK)
            return displayName;

        final BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) Minecraft.getInstance().objectMouseOver;
        final BlockState state = Minecraft.getInstance().world.getBlockState(rayTraceResult.getPos());
        final BlockBitInfo.SupportsAnalysisResult result = BlockBitInfo.doSupportAnalysis(state);
        return new StringTextComponent(
          result.isSupported() ?
            TextFormatting.GREEN + result.getSupportedReason().getLocal() + TextFormatting.RESET :
            TextFormatting.RED + result.getUnsupportedReason().getLocal() + TextFormatting.RESET
        );
    }

    @Override
	@OnlyIn( Dist.CLIENT )
	public void addInformation(
			final ItemStack stack,
			final World worldIn,
			final List<ITextComponent> tooltip,
			final ITooltipFlag advanced )
	{
		super.addInformation( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpMagnifyingGlass, tooltip );
	}
}
