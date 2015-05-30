
package mod.chiselsandbits.render.BlockChisled;

import java.util.WeakHashMap;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobState;
import mod.chiselsandbits.render.BaseSmartModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;


@SuppressWarnings( "deprecation" )
public class ChisledBlockSmartModel extends BaseSmartModel implements ISmartItemModel, ISmartBlockModel
{

	private final WeakHashMap<VoxelBlobState, ChisledBlockBaked> modelCache = new WeakHashMap<VoxelBlobState, ChisledBlockBaked>();
	private final WeakHashMap<ItemStack, IBakedModel> itemToModel = new WeakHashMap<ItemStack, IBakedModel>();

	private IBakedModel getCachedModel(
			final Integer blockP,
			final VoxelBlobState data )
	{
		if ( data == null )
			return new ChisledBlockBaked( blockP, data );

		ChisledBlockBaked out = modelCache.get( data );

		if ( out == null )
		{
			out = new ChisledBlockBaked( blockP, data );
			modelCache.put( data, out );
		}

		return out;
	}

	@Override
	public IBakedModel handleBlockState(
			final IBlockState state )
	{
		final IExtendedBlockState myState = ( IExtendedBlockState ) state;

		final VoxelBlobState data = myState.getValue( BlockChiseled.v_prop );
		Integer blockP = myState.getValue( BlockChiseled.block_prop );

		blockP = blockP == null ? 0 : blockP;

		return getCachedModel( blockP, data );
	}

	@Override
	public IBakedModel handleItemState(
			final ItemStack stack )
	{
		IBakedModel mdl;
		mdl = itemToModel.get( stack );

		if ( mdl != null )
			return mdl;

		NBTTagCompound c = stack.getTagCompound();
		if ( c == null )
			return this;

		c = c.getCompoundTag( "BlockEntityTag" );
		if ( c == null )
			return this;

		final byte[] data = c.getByteArray( "v" );
		final Integer blockP = c.getInteger( "b" );

		itemToModel.put( stack, mdl = getCachedModel( blockP, new VoxelBlobState( data, 0L ) ) );

		return mdl;
	}

}
