package mod.chiselsandbits.items;

import java.util.List;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.integration.mcmultipart.MCMultipartProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemWrench extends Item
{

	public ItemWrench()
	{
		setMaxStackSize( 1 );

		final long uses = ChiselsAndBits.getConfig().wrenchUses;
		setMaxDamage( ChiselsAndBits.getConfig().damageTools ? (int) Math.max( 0, Math.min( Short.MAX_VALUE, uses ) ) : 0 );
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
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpWrench, tooltip,
				ClientSide.instance.getKeyName( Minecraft.getMinecraft().gameSettings.keyBindUseItem ) );
	}

	@Override
	public EnumActionResult onItemUse(
			final EntityPlayer player,
			final World world,
			final BlockPos pos,
			final EnumHand hand,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		final ItemStack stack = player.getHeldItem( hand );

		if ( !player.canPlayerEdit( pos, side, stack ) || !world.isBlockModifiable( player, pos ) )
		{
			return EnumActionResult.FAIL;
		}

		final IBlockState b = world.getBlockState( pos );
		if ( b != null && !player.isSneaking() )
		{
			if ( MCMultipartProxy.proxyMCMultiPart.isMultiPartTileEntity( world, pos ) )
			{
				if ( MCMultipartProxy.proxyMCMultiPart.rotate( world, pos, player ) )
				{
					return EnumActionResult.SUCCESS;
				}
			}

			if ( b.getBlock().rotateBlock( world, pos, side ) )
			{
				stack.damageItem( 1, player );
				world.notifyNeighborsOfStateChange( pos, b.getBlock(), false );
				player.swingArm( hand );
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.FAIL;
	}

}