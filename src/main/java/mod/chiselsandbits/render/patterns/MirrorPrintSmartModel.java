package mod.chiselsandbits.render.patterns;

import java.util.WeakHashMap;

import org.lwjgl.input.Keyboard;

import mod.chiselsandbits.render.BaseSmartModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ISmartItemModel;

public class MirrorPrintSmartModel extends BaseSmartModel implements ISmartItemModel
{

	WeakHashMap<ItemStack, MirrorPrintBaked> cache = new WeakHashMap<ItemStack, MirrorPrintBaked>();

	@Override
	public IBakedModel handleItemState(
			final ItemStack stack )
	{
		if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
		{
			MirrorPrintBaked npb = cache.get( stack );

			if ( npb == null )
			{
				cache.put( stack, npb = new MirrorPrintBaked( stack ) );
			}

			return npb;
		}

		return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel( new ModelResourceLocation( "chiselsandbits:mirrorprint_written", "inventory" ) );
	}

}
