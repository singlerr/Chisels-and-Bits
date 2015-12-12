
package mod.chiselsandbits.render.patterns;

import java.util.WeakHashMap;

import org.lwjgl.input.Keyboard;

import mod.chiselsandbits.render.BaseSmartModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ISmartItemModel;

@SuppressWarnings( "deprecation" )
public class NegativePrintSmartModel extends BaseSmartModel implements ISmartItemModel
{

	WeakHashMap<ItemStack, NegativePrintBaked> cache = new WeakHashMap<ItemStack, NegativePrintBaked>();

	@Override
	public IBakedModel handleItemState(
			final ItemStack stack )
	{
		if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
		{
			NegativePrintBaked npb = cache.get( stack );

			if ( npb == null )
			{
				cache.put( stack, npb = new NegativePrintBaked( stack ) );
			}

			return npb;
		}

		return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel( new ModelResourceLocation( "chiselsandbits:negativeprint_written", "inventory" ) );
	}
}
