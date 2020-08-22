package mod.chiselsandbits.items;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ItemWrench extends Item
{

	public ItemWrench(Item.Properties properties)
	{
	    super(properties.maxStackSize(1).maxDamage(ChiselsAndBits.getConfig().getServer().damageTools.get() ? (int) Math.max( 0, Math.min( Short.MAX_VALUE, ChiselsAndBits.getConfig().getServer().wrenchUses.get() ) ) : 0));
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
		ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpWrench, tooltip,
				ClientSide.instance.getKeyName( Minecraft.getInstance().gameSettings.keyBindUseItem ) );
	}

    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        final PlayerEntity player = context.getPlayer();
        final BlockPos pos = context.getPos();
        final Direction side = context.getFace();
        final World world = context.getWorld();
        final ItemStack stack = context.getItem();
        final Hand hand = context.getHand();

        if ( !player.canPlayerEdit( pos, side, stack ) || !world.isBlockModifiable( player, pos ) )
        {
            return ActionResultType.FAIL;
        }

        final BlockState b = world.getBlockState( pos );
        if ( b != null && !player.isSneaking() )
        {
            BlockState nb;
            if ((nb = b.rotate( world, pos, Rotation.CLOCKWISE_90 )) != b )
            {
                world.setBlockState(pos, nb);
                stack.damageItem(1, player, playerEntity -> {
                    playerEntity.sendBreakAnimation(hand);
                });
                world.notifyNeighborsOfStateChange( pos, b.getBlock() );
                player.swingArm( hand );
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.FAIL;
    }
}