package mod.chiselsandbits.render.patterns;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.render.BaseBakedItemModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class PrintBaked extends BaseBakedItemModel
{

	final String itemName;

	public PrintBaked(
			final String itname,
			final IPatternItem item,
			final ItemStack stack )
	{
		itemName = itname;

		final ItemStack blockItem = item.getPatternedItem( stack );
		final IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel( blockItem );

		for ( final EnumFacing face : EnumFacing.VALUES )
		{
			list.addAll( model.getFaceQuads( face ) );
		}

		list.addAll( model.getGeneralQuads() );
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite( ChiselsAndBits.MODID + ":item/" + itemName );
	}
}
