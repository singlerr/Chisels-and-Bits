
package mod.chiselsandbits.render.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mod.chiselsandbits.ChiselsAndBits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;

@SuppressWarnings( "deprecation" )
public class PositivePrintBaked implements IFlexibleBakedModel
{

	ArrayList<BakedQuad> list = new ArrayList<BakedQuad>();

	public PositivePrintBaked(
			final ItemStack stack )
	{
		final ItemStack blockItem = ChiselsAndBits.instance.itemPositiveprint.getPatternedItem( stack );
		final IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel( blockItem );

		for ( final EnumFacing face : EnumFacing.VALUES )
		{
			list.addAll( model.getFaceQuads( face ) );
		}

		list.addAll( model.getGeneralQuads() );
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite( ChiselsAndBits.MODID + ":item/positiveprint" );
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public List<BakedQuad> getFaceQuads(
			final EnumFacing side )
	{
		return Collections.emptyList();
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		return list;
	}

	@Override
	public VertexFormat getFormat()
	{
		return null;
	}

}
