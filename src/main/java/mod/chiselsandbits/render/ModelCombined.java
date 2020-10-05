package mod.chiselsandbits.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import mod.chiselsandbits.client.model.baked.BaseBakedBlockModel;
import mod.chiselsandbits.core.ClientSide;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModelCombined extends BaseBakedBlockModel
{

    private static final Random COMBINED_RANDOM_MODEL = new Random();

	IBakedModel[] merged;

	List<BakedQuad>[] face;
	List<BakedQuad>   generic;

	boolean isSideLit;

	@SuppressWarnings( "unchecked" )
	public ModelCombined(
			final IBakedModel... args )
	{
		face = new ArrayList[Direction.values().length];

		generic = new ArrayList<>();
		for ( final Direction f : Direction.values() )
		{
			face[f.ordinal()] = new ArrayList<>();
		}

		merged = args;

		for ( final IBakedModel m : merged )
		{
			generic.addAll( m.getQuads( null, null, COMBINED_RANDOM_MODEL ) );
			for ( final Direction f : Direction.values() )
			{
				face[f.ordinal()].addAll( m.getQuads( null, f, COMBINED_RANDOM_MODEL ) );
			}
		}

		isSideLit = Arrays.stream(args).anyMatch(IBakedModel::isSideLit);
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		for ( final IBakedModel a : merged )
		{
			return a.getParticleTexture();
		}

		return ClientSide.instance.getMissingIcon();
	}

    @NotNull
    @Override
    public List<BakedQuad> getQuads(
      @Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand, @NotNull final IModelData extraData)
    {
        if ( side != null )
        {
            return face[side.ordinal()];
        }

        return generic;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final Random rand)
    {
        if ( side != null )
        {
            return face[side.ordinal()];
        }

        return generic;
    }

    @Override
    public boolean isSideLit()
    {
        return isSideLit;
    }
}
