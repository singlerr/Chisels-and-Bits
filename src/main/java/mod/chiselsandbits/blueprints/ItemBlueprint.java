package mod.chiselsandbits.blueprints;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import mod.chiselsandbits.client.gui.ModGuiTypes;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.DeprecationHelper;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketOpenGui;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlueprint extends Item implements Runnable
{

	final private String NBT_SIZE_X = "xSize";
	final private String NBT_SIZE_Y = "ySize";
	final private String NBT_SIZE_Z = "zSize";

	public ItemBlueprint()
	{
		setMaxStackSize( 1 );
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(
			final ItemStack itemStackIn,
			final World worldIn,
			final EntityPlayer playerIn,
			final EnumHand hand )
	{
		if ( worldIn.isRemote )
		{
			NetworkRouter.instance.sendToServer( new PacketOpenGui( ModGuiTypes.Blueprint ) );
		}

		return new ActionResult<ItemStack>( EnumActionResult.SUCCESS, itemStackIn );
	}

	@Override
	public String getItemStackDisplayName(
			final ItemStack stack )
	{
		if ( isWritten( stack ) )
		{
			return DeprecationHelper.translateToLocal( "item.mod.chiselsandbits.blueprint_written.name" );
		}

		return super.getItemStackDisplayName( stack );
	}

	@Override
	public EnumActionResult onItemUse(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final World worldIn,
			BlockPos pos,
			final EnumHand hand,
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
				final NBTTagCompound tag = stack.getTagCompound();
				e.setSize( tag.getInteger( NBT_SIZE_X ), tag.getInteger( NBT_SIZE_Y ), tag.getInteger( NBT_SIZE_Z ) );
				worldIn.spawnEntityInWorld( e );
			}

			stack.stackSize--;
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.FAIL;
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

	@Override
	public void run()
	{
		while ( true )
		{
			try
			{
				synchronized ( this )
				{
					for ( final Entry<String, BlueprintData> a : data.entrySet() )
					{
						if ( a.getValue().isExpired() )
						{
							data.remove( a.getKey() );
						}
					}
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
	private final Map<String, BlueprintData> data = new HashMap<String, BlueprintData>();
	private Thread cleanThread;

	@SideOnly( Side.CLIENT )
	synchronized private BlueprintData getData(
			final String url )
	{
		if ( data.containsKey( url ) )
		{
			return data.get( url );
		}

		if ( cleanThread != null )
		{
			cleanThread = new Thread( this );
			cleanThread.setName( "BlueprintCleanup" );
			cleanThread.start();
		}

		final BlueprintData dat = new BlueprintData( url );
		data.put( url, dat );
		return dat;
	}

}
