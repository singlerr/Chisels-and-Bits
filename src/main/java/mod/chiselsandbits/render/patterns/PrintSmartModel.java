package mod.chiselsandbits.render.patterns;

import java.util.WeakHashMap;

import org.lwjgl.input.Keyboard;

import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.render.BaseSmartModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ISmartItemModel;

public class PrintSmartModel extends BaseSmartModel implements ISmartItemModel
{

	WeakHashMap<ItemStack, PrintBaked> cache = new WeakHashMap<ItemStack, PrintBaked>();

	final IPatternItem item;
	final String name;

	public PrintSmartModel(
			final String name,
			final IPatternItem item )
	{
		this.name = name;
		this.item = item;
	}

	@Override
	public IBakedModel handleItemState(
			final ItemStack stack )
	{
		if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
		{
			PrintBaked npb = cache.get( stack );

			if ( npb == null )
			{
				cache.put( stack, npb = new PrintBaked( name, item, stack ) );
			}

			return npb;
		}

		return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel( new ModelResourceLocation( "chiselsandbits:" + name + "_written", "inventory" ) );
	}

}
