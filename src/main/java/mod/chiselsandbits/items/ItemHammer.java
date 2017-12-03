package mod.chiselsandbits.items;

import java.util.List;

import mod.chiselsandbits.blueprints.EntityBlueprint;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemHammer extends Item
{

	public ItemHammer()
	{
		setMaxStackSize( 1 );
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(
			World world,
			EntityPlayer player,
			EnumHand hand )
	{
		return new ActionResult<ItemStack>( findAndInteractWithBlueprint( player, world, hand ), player.getHeldItem( hand ) );
	}

	@Override
	public EnumActionResult onItemUseFirst(
			EntityPlayer player,
			World world,
			BlockPos pos,
			EnumFacing side,
			float hitX,
			float hitY,
			float hitZ,
			EnumHand hand )
	{
		return findAndInteractWithBlueprint( player, world, hand );
	}

	public EnumActionResult findAndInteractWithBlueprint(
			EntityPlayer player,
			World world,
			EnumHand hand )
	{
		EnumActionResult result = EnumActionResult.FAIL;

		for ( EntityBlueprint x : world.getEntitiesWithinAABB( EntityBlueprint.class, player.getEntityBoundingBox().expand( 128, 128, 128 ) ) )
		{
			if ( x.getBlueprintBox().expand( 2, 2, 2 ).intersectsWith( player.getEntityBoundingBox() ) )
			{
				x.hitWithTool( player, player.getHeldItem( hand ) );
				result = EnumActionResult.SUCCESS;
			}
		}

		return result;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void addInformation(
			final ItemStack stack,
			final World worldIn,
			final List<String> tooltip,
			final ITooltipFlag advanced )
	{
		super.addInformation( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpHammer, tooltip );
	}

}
