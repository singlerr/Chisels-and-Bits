package mod.chiselsandbits.debug;

import mod.chiselsandbits.debug.DebugAction.Tests;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

public class ItemApiDebug extends Item
{

	public ItemApiDebug(Item.Properties properties)
	{
	    super(properties.maxDamage(1).maxStackSize(1));
	}

    @Override
    public ITextComponent getDisplayName(final ItemStack stack)
    {
        final ITextComponent parent = super.getDisplayName(stack);
        if (!(parent instanceof IFormattableTextComponent))
            return parent;

        final IFormattableTextComponent name = (IFormattableTextComponent) parent;
        return name.appendString(" - " + getAction( stack ).name());
    }

	private Tests getAction(
			final ItemStack stack )
	{
		return Tests.values()[getActionID( stack )];
	}

    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        final ItemStack stack = context.getPlayer().getHeldItem( context.getHand() );

        if ( context.getPlayer().isSneaking() )
        {
            final int newDamage = getActionID( stack ) + 1;
            setActionID( stack, newDamage % Tests.values().length );
            DebugAction.Msg( context.getPlayer(), getAction( stack ).name() );
            return ActionResultType.SUCCESS;
        }

        getAction( stack ).which.run( context.getWorld(), context.getPos(), context.getFace(), context.getHitVec().x, context.getHitVec().y, context.getHitVec().z, context.getPlayer() );
        return ActionResultType.SUCCESS;
    }

	private void setActionID(
			final ItemStack stack,
			final int i )
	{
		final CompoundNBT o = new CompoundNBT();
		o.putInt( "id", i );
		stack.setTag( o );
	}

	private int getActionID(
			final ItemStack stack )
	{
		if ( stack.hasTag() )
		{
			return ModUtil.getTagCompound( stack ).getInt( "id" );
		}

		return 0;
	}

}
