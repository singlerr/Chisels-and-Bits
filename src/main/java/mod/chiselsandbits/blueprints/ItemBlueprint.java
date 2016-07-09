package mod.chiselsandbits.blueprints;

import java.util.HashMap;

import mod.chiselsandbits.client.gui.ModGuiTypes;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketOpenGui;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlueprint extends Item implements Runnable
{

	public ItemBlueprint()
	{
		setMaxStackSize( 1 );
	}

	@Override
	public ItemStack onItemRightClick(
			final ItemStack itemStackIn,
			final World worldIn,
			final EntityPlayer playerIn )
	{
		if ( worldIn.isRemote )
		{
			NetworkRouter.instance.sendToServer( new PacketOpenGui( ModGuiTypes.Blueprint ) );
		}

		return itemStackIn;
	}

	@Override
	public String getItemStackDisplayName(
			final ItemStack stack )
	{
		if ( isWritten( stack ) )
		{
			return ( "" + StatCollector.translateToLocal( "item.mod.chiselsandbits.blueprint_written.name" ) ).trim();
		}

		return super.getItemStackDisplayName( stack );
	}

	@Override
	public boolean onItemUse(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final World worldIn,
			BlockPos pos,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		if ( isWritten( stack ) )
		{
			if ( !worldIn.isRemote )
			{
				final IBlockState state = worldIn.getBlockState( pos );
				if ( !state.getBlock().isReplaceable( worldIn, pos ) )
				{
					pos = pos.offset( side );
				}

				final EntityBlueprint e = new EntityBlueprint( worldIn );
				e.posX = pos.getX() + 0.5;
				e.posY = pos.getY() + 0.5;
				e.posZ = pos.getZ() + 0.5;
				e.setItemStack( stack.copy() );
				worldIn.spawnEntityInWorld( e );
			}

			stack.stackSize--;
			return true;
		}

		return false;
	}

	public boolean isWritten(
			final ItemStack stack )
	{
		if ( stack.hasTagCompound() )
		{
			return verifyDataSource( stack.getTagCompound() );
		}

		return false;
	}

	private boolean verifyDataSource(
			final NBTTagCompound tagCompound )
	{
		if ( !tagCompound.hasKey( "xSize" ) || !tagCompound.hasKey( "ySize" ) || !tagCompound.hasKey( "zSize" ) )
		{
			return false;
		}

		if ( tagCompound.hasKey( "data" ) )
		{
			return true;
		}

		if ( tagCompound.hasKey( "url" ) )
		{
			final BlueprintData data = getData( tagCompound.getString( "url" ) );
			if ( data != null )
			{
				return data.getState().readyOrWaiting();
			}
		}

		return false;
	}

	HashMap<String, BlueprintData> data;

	@Override
	public void run()
	{
		while ( true )
		{
			try
			{
				synchronized ( this )
				{

				}
				Thread.sleep( 5000 );
			}
			catch ( final InterruptedException e )
			{
				Log.logError( "Error Pruning Blueprint Data!", e );
			}
		}
	}

	@SideOnly( Side.CLIENT )
	synchronized private BlueprintData getData(
			final String string )
	{
		// TODO Auto-generated method stub
		return null;
	}

}
