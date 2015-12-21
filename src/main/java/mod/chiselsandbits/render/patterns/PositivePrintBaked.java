package mod.chiselsandbits.render.patterns;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.render.BaseBakedItemModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class PositivePrintBaked extends BaseBakedItemModel
{

	public PositivePrintBaked(
			final ItemStack stack )
	{
		final ItemStack blockItem = ChiselsAndBits.instance.items.itemPositiveprint.getPatternedItem( stack );
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
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite( ChiselsAndBits.MODID + ":item/positiveprint" );
	}

}
