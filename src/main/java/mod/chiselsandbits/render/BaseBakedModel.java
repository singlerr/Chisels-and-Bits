package mod.chiselsandbits.render;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.TRSRTransformation;

@SuppressWarnings( "deprecation" )
public abstract class BaseBakedModel implements IFlexibleBakedModel, IPerspectiveAwareModel
{

	private final static Matrix4f identity;
	private final static Matrix4f thirdPerson;

	static
	{/*
		 * "rotation": [ 10, -45, 170 ],
		 *
		 * "translation": [ 0, 1.5, -2.75 ]
		 *
		 * "scale": [ 0.375, 0.375, 0.375 ] } }
		 */
		final Vector3f translation = new Vector3f( 0, 1.5f / 16.0f, -2.75f / 16.0f );
		final Vector3f scale = new Vector3f( 0.375f, 0.375f, 0.375f );
		final Quat4f rotation = TRSRTransformation.quatFromYXZDegrees( new Vector3f( 10.0f, -45.0f, 170.0f ) );

		final TRSRTransformation transform = new TRSRTransformation( translation, rotation, scale, null );
		thirdPerson = transform.getMatrix();

		// yerp.
		identity = new Matrix4f();
		identity.setIdentity();
	}

	@Override
	public Pair<IBakedModel, Matrix4f> handlePerspective(
			final TransformType cameraTransformType )
	{
		switch ( cameraTransformType )
		{
			case THIRD_PERSON:
				return new ImmutablePair<IBakedModel, Matrix4f>( this, thirdPerson );
			default:
				return new ImmutablePair<IBakedModel, Matrix4f>( this, identity );
		}

	}
}
