package mod.chiselsandbits.render.patterns;

import java.util.WeakHashMap;

import mod.chiselsandbits.render.BaseSmartModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ISmartItemModel;

import org.lwjgl.input.Keyboard;

public class PositivePrintSmartModel extends BaseSmartModel implements ISmartItemModel
{

	WeakHashMap<ItemStack, PositivePrintBaked> cache = new WeakHashMap<ItemStack, PositivePrintBaked>();

	@Override
	public IBakedModel handleItemState(
			final ItemStack stack )
	{
		if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
		{
			PositivePrintBaked npb = cache.get( stack );

			if ( npb == null )
			{
				cache.put( stack, npb = new PositivePrintBaked( stack ) );
			}

			return npb;
		}

		return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel( new ModelResourceLocation( "chiselsandbits:positiveprint_written", "inventory" ) );
	}

}
