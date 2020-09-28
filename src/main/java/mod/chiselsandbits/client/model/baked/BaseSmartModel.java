package mod.chiselsandbits.client.model.baked;

import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.render.NullBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public abstract class BaseSmartModel implements IBakedModel
{

	private final ItemOverrideList overrides;

	private static class OverrideHelper extends ItemOverrideList
	{
		final BaseSmartModel parent;

		public OverrideHelper(
				final BaseSmartModel p )
		{
			super();
			parent = p;
		}

        @Nullable
        @Override
        public IBakedModel func_239290_a_(
          final IBakedModel p_239290_1_, final ItemStack p_239290_2_, @Nullable final ClientWorld p_239290_3_, @Nullable final LivingEntity p_239290_4_)
        {
            return parent.func_239290_a_( p_239290_1_, p_239290_2_, p_239290_3_, p_239290_4_ );
        }

	};

	public BaseSmartModel()
	{
		overrides = new OverrideHelper( this );
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
		final TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture( Blocks.STONE.getDefaultState() );

		if ( sprite == null )
		{
			return ClientSide.instance.getMissingIcon();
		}

		return sprite;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

    @NotNull
    @Override
    public List<BakedQuad> getQuads(
      @Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand, @NotNull final IModelData extraData)
    {
        final IBakedModel model = handleBlockState( state, rand, extraData );
        return model.getQuads( state, side, rand, extraData );
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final Random rand)
    {
        final IBakedModel model = handleBlockState( state, rand );
        return model.getQuads( state, side, rand );
    }

	public IBakedModel handleBlockState(
			final BlockState state,
			final Random rand )
	{
		return NullBakedModel.instance;
	}

	public IBakedModel handleBlockState(
	  final BlockState state,
      final Random random,
      final IModelData modelData
    )
    {
        return NullBakedModel.instance;
    }

	@Override
	public ItemOverrideList getOverrides()
	{
		return overrides;
	}

	public IBakedModel func_239290_a_(
			final IBakedModel originalModel,
			final ItemStack stack,
			final World world,
			final LivingEntity entity )
	{
		return originalModel;
	}

}
