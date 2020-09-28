package mod.chiselsandbits.render.patterns;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.client.model.baked.BaseBakedItemModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class PrintBaked extends BaseBakedItemModel
{

	final String itemName;

	public PrintBaked(
			final String itname,
			final IPatternItem item,
			final ItemStack stack )
	{
		itemName = itname;

		final ItemStack blockItem = item.getPatternedItem( stack, false );
		IBakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelMesher().getItemModel( blockItem );

		model = model.getOverrides().func_239290_a_( model, blockItem, null, null );

		for ( final Direction face : Direction.values() )
		{
			list.addAll( model.getQuads( null, face, RANDOM ) );
		}

		list.addAll( model.getQuads( null, null, RANDOM) );
	}

    @Override
    public boolean func_230044_c_()
    {
        return false;
    }

    @Override
	public TextureAtlasSprite getParticleTexture()
	{
		return Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply( new ResourceLocation(ChiselsAndBits.MODID,"item/" + itemName ));
	}
}
