package mod.chiselsandbits.client.model.baked;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.chiselsandbits.helpers.IStateRef;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Random;

public abstract class BaseBakedPerspectiveModel implements IBakedModel
{

    protected static final Random RANDOM = new Random();

	private static final TransformationMatrix ground;
	private static final TransformationMatrix gui;
	private static final TransformationMatrix fixed;
	private static final TransformationMatrix firstPerson_righthand;
	private static final TransformationMatrix firstPerson_lefthand;
	private static final TransformationMatrix thirdPerson_righthand;
	private static final TransformationMatrix thirdPerson_lefthand;

	static
	{
		gui = getMatrix( 0, 0, 0, 30, 225, 0, 0.625f );
		ground = getMatrix( 0, 3 / 16.0f, 0, 0, 0, 0, 0.25f );
		fixed = getMatrix( 0, 0, 0, 0, 0, 0, 0.5f );
		thirdPerson_lefthand = thirdPerson_righthand = getMatrix( 0, 2.5f / 16.0f, 0, 75, 45, 0, 0.375f );
		firstPerson_righthand = firstPerson_lefthand = getMatrix( 0, 0, 0, 0, 45, 0, 0.40f );
	}

	private static TransformationMatrix getMatrix(
			final float transX,
			final float transY,
			final float transZ,
			final float rotX,
			final float rotY,
			final float rotZ,
			final float scaleXYZ )
	{
		final Vector3f translation = new Vector3f( transX, transY, transZ );
		final Vector3f scale = new Vector3f( scaleXYZ, scaleXYZ, scaleXYZ );
		final Quaternion rotation = new Quaternion(rotX, rotY, rotZ, true);

		return new TransformationMatrix(translation, rotation, scale, null);
	}

    @Override
    public IBakedModel handlePerspective(final ItemCameraTransforms.TransformType cameraTransformType, final MatrixStack mat)
    {
        switch ( cameraTransformType )
        {
            case FIRST_PERSON_LEFT_HAND:
                firstPerson_lefthand.push(mat);
                return this;
            case FIRST_PERSON_RIGHT_HAND:
                firstPerson_righthand.push(mat);
                return this;
            case THIRD_PERSON_LEFT_HAND:
                thirdPerson_lefthand.push(mat);
                return this;
            case THIRD_PERSON_RIGHT_HAND:
                thirdPerson_righthand.push(mat);
            case FIXED:
                fixed.push(mat);
                return this;
            case GROUND:
                ground.push(mat);
                return this;
            case GUI:
                gui.push(mat);
                return this;
            default:
        }

        fixed.push(mat);
        return this;
    }
}
