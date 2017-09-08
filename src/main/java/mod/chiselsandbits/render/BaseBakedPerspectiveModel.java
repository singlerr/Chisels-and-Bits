package mod.chiselsandbits.render;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraftforge.common.model.TRSRTransformation;

public abstract class BaseBakedPerspectiveModel implements IBakedModel
{

	private static final Matrix4f ground;
	private static final Matrix4f gui;
	private static final Matrix4f fixed;
	private static final Matrix4f firstPerson_righthand;
	private static final Matrix4f firstPerson_lefthand;
	private static final Matrix4f thirdPerson_righthand;
	private static final Matrix4f thirdPerson_lefthand;

	static
	{
		gui = getMatrix( 0, 0, 0, 30, 225, 0, 0.625f );
		ground = getMatrix( 0, 3 / 16.0f, 0, 0, 0, 0, 0.25f );
		fixed = getMatrix( 0, 0, 0, 0, 0, 0, 0.5f );
		thirdPerson_lefthand = thirdPerson_righthand = getMatrix( 0, 2.5f / 16.0f, 0, 75, 45, 0, 0.375f );
		firstPerson_righthand = getMatrix( 0, 0, 0, 0, 45, 0, 0.40f );
		firstPerson_lefthand = getMatrix( 0, 0, 0, 0, 0, 225, 0.40f );
	}

	private static Matrix4f getMatrix(
			final float transX,
			final float transY,
			final float transZ,
			final float rotX,
			final float rotY,
			final float rotZ,
			final float scaleXYZ )
	{
		final javax.vecmath.Vector3f translation = new javax.vecmath.Vector3f( transX, transY, transZ );
		final javax.vecmath.Vector3f scale = new javax.vecmath.Vector3f( scaleXYZ, scaleXYZ, scaleXYZ );
		final Quat4f rotation = TRSRTransformation.quatFromXYZDegrees( new javax.vecmath.Vector3f( rotX, rotY, rotZ ) );

		final TRSRTransformation transform = new TRSRTransformation( translation, rotation, scale, null );
		return transform.getMatrix();
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
			final TransformType cameraTransformType )
	{
		switch ( cameraTransformType )
		{
			case FIRST_PERSON_LEFT_HAND:
				return new ImmutablePair<IBakedModel, Matrix4f>( this, firstPerson_lefthand );
			case FIRST_PERSON_RIGHT_HAND:
				return new ImmutablePair<IBakedModel, Matrix4f>( this, firstPerson_righthand );
			case THIRD_PERSON_LEFT_HAND:
				return new ImmutablePair<IBakedModel, Matrix4f>( this, thirdPerson_lefthand );
			case THIRD_PERSON_RIGHT_HAND:
				return new ImmutablePair<IBakedModel, Matrix4f>( this, thirdPerson_righthand );
			case FIXED:
				return new ImmutablePair<IBakedModel, Matrix4f>( this, fixed );
			case GROUND:
				return new ImmutablePair<IBakedModel, Matrix4f>( this, ground );
			case GUI:
				return new ImmutablePair<IBakedModel, Matrix4f>( this, gui );
			default:
		}

		return new ImmutablePair<IBakedModel, Matrix4f>( this, fixed );
	}

}
