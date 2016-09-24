package ebon.cameras;

import ebon.celestial.manager.*;
import ebon.options.*;
import flounder.engine.*;
import flounder.engine.entrance.*;
import flounder.inputs.*;
import flounder.maths.*;
import flounder.maths.matrices.*;
import flounder.maths.vectors.*;
import flounder.space.*;
import sun.reflect.generics.reflectiveObjects.*;

public class CameraFPS implements ICamera {
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = (float) GalaxyManager.GALAXY_RADIUS * 4.0f;
	private static final float FIELD_OF_VIEW = 70.0f;

	private static final float ROTATE_AGILITY = 6.0f;
	private static final float PITCH_AGILITY = 8.0f;

	private final static float MAX_ANGLE_OF_ELEVATION = -1.0f;
	private final static float MIN_ANGLE_OF_ELEVATION = 1.0f;

	private final static float INFLUENCE_OF_MOUSEDY = -175.0f;
	private final static float INFLUENCE_OF_MOUSEDX = INFLUENCE_OF_MOUSEDY * 92.0f;
	private final static float INFLUENCE_OF_JOYSTICKDY = -1.0f;
	private final static float INFLUENCE_OF_JOYSTICKDX = 100.0f * INFLUENCE_OF_JOYSTICKDY;

	private Vector3f reusableViewVector;

	private Vector3f position;
	private Vector3f rotation;
	private Frustum viewFrustum;
	private Matrix4f viewMatrix;

	private JoystickAxis joystickRotateX;
	private JoystickAxis joystickRotateY;

	private float angleOfElevation;
	private float angleAroundPlayer;

	private Vector3f targetPosition;
	private Vector3f targetRotation;
	private float targetElevation;
	private float targetRotationAngle;

	@Override
	public void init() {
		this.reusableViewVector = new Vector3f();

		this.position = new Vector3f();
		this.rotation = new Vector3f();
		this.viewFrustum = new Frustum();
		this.viewMatrix = new Matrix4f();

		this.joystickRotateX = new JoystickAxis(OptionsControls.JOYSTICK_PORT, OptionsControls.JOYSTICK_ROTATE_X);
		this.joystickRotateY = new JoystickAxis(OptionsControls.JOYSTICK_PORT, OptionsControls.JOYSTICK_ROTATE_Y);

		this.angleOfElevation = 0.0f;
		this.angleAroundPlayer = 0.0f;

		this.targetPosition = new Vector3f();
		this.targetRotation = new Vector3f();
		this.targetElevation = 0.0f;
		this.targetRotationAngle = 0.0f;
	}

	@Override
	public float getNearPlane() {
		return NEAR_PLANE;
	}

	@Override
	public float getFarPlane() {
		return FAR_PLANE;
	}

	@Override
	public float getFOV() {
		return FIELD_OF_VIEW;
	}

	@Override
	public void update(Vector3f focusPosition, Vector3f focusRotation, boolean gamePaused) {
		calculateHorizontalAngle(gamePaused);
		calculateVerticalAngle(gamePaused);

		this.targetPosition.set(focusPosition);
		this.targetRotation.set(focusRotation);

		updateHorizontalAngle();
		updatePitchAngle();
		calculatePosition();

		if (FlounderEngine.getProfiler().isOpen()) {
			FlounderEngine.getProfiler().add("MainCamera", "Angle Of Elevation", angleOfElevation);
			FlounderEngine.getProfiler().add("MainCamera", "Rotation", rotation);
			FlounderEngine.getProfiler().add("MainCamera", "Position", position);
			FlounderEngine.getProfiler().add("MainCamera", "Angle Around MainPlayer", angleAroundPlayer);
			FlounderEngine.getProfiler().add("MainCamera", "Target Elevation", targetElevation);
			FlounderEngine.getProfiler().add("MainCamera", "Target Rotation Angle", targetRotationAngle);
		}
	}

	private void calculateHorizontalAngle(boolean gamePaused) {
		float change = 0.0f;

		if (!gamePaused) {
			if (FlounderEngine.getDevices().getJoysticks().isConnected(0)) {
				if (Math.abs(Maths.deadband(0.01f, joystickRotateX.getAmount())) > 0.0f) {
					change = INFLUENCE_OF_JOYSTICKDX * FlounderEngine.getDelta() * -joystickRotateX.getAmount();
				}
			} else {
				change = FlounderEngine.getDevices().getMouse().getDeltaX() * INFLUENCE_OF_MOUSEDX;
			}
		}

		targetRotationAngle -= change;

		if (targetRotationAngle >= Maths.DEGREES_IN_HALF_CIRCLE) {
			targetRotationAngle -= Maths.DEGREES_IN_CIRCLE;
		} else if (targetRotationAngle <= -Maths.DEGREES_IN_HALF_CIRCLE) {
			targetRotationAngle += Maths.DEGREES_IN_CIRCLE;
		}
	}

	private void calculateVerticalAngle(boolean gamePaused) {
		float delta = FlounderEngine.getDelta();
		float change = 0.0f;

		if (!gamePaused) {
			if (FlounderEngine.getDevices().getJoysticks().isConnected(0)) {
				if (Math.abs(Maths.deadband(0.01f, joystickRotateY.getAmount())) > 0.0f) {
					change = -joystickRotateY.getAmount() * delta * INFLUENCE_OF_JOYSTICKDY;
				}
			} else {
				change = -FlounderEngine.getDevices().getMouse().getDeltaY() * INFLUENCE_OF_MOUSEDY;
			}
		}

		targetElevation -= change;

		if (targetElevation <= MAX_ANGLE_OF_ELEVATION) {
			targetElevation = MAX_ANGLE_OF_ELEVATION;
		} else if (targetElevation >= MIN_ANGLE_OF_ELEVATION) {
			targetElevation = MIN_ANGLE_OF_ELEVATION;
		}
	}

	private void updateHorizontalAngle() {
		float offset = targetRotationAngle - angleAroundPlayer;

		if (Math.abs(offset) > Maths.DEGREES_IN_HALF_CIRCLE) {
			if (offset < 0) {
				offset = targetRotationAngle + Maths.DEGREES_IN_CIRCLE - angleAroundPlayer;
			} else {
				offset = targetRotationAngle - Maths.DEGREES_IN_CIRCLE - angleAroundPlayer;
			}
		}

		float change = ROTATE_AGILITY * FlounderEngine.getDelta() * offset;
		angleAroundPlayer += change;

		if (angleAroundPlayer >= Maths.DEGREES_IN_HALF_CIRCLE) {
			angleAroundPlayer -= Maths.DEGREES_IN_CIRCLE;
		} else if (angleAroundPlayer <= -Maths.DEGREES_IN_HALF_CIRCLE) {
			angleAroundPlayer += Maths.DEGREES_IN_CIRCLE;
		}
	}

	private void updatePitchAngle() {
		float offset = targetElevation - angleOfElevation;
		float change = PITCH_AGILITY * FlounderEngine.getDelta() * offset;
		angleOfElevation += change;
	}

	private void calculatePosition() {
		position.set(targetPosition);
		rotation.set((float) Math.toDegrees(angleOfElevation), Maths.DEGREES_IN_HALF_CIRCLE + angleAroundPlayer, targetRotation.z);
	}

	private void updateViewMatrix() {
		viewMatrix.setIdentity();
		position.negate();
		Matrix4f.rotate(viewMatrix, reusableViewVector.set(1.0f, 0.0f, 0.0f), (float) Math.toRadians(rotation.x), viewMatrix);
		Matrix4f.rotate(viewMatrix, reusableViewVector.set(0.0f, 1.0f, 0.0f), (float) Math.toRadians(-rotation.y), viewMatrix);
		Matrix4f.rotate(viewMatrix, reusableViewVector.set(0.0f, 0.0f, 1.0f), (float) Math.toRadians(rotation.z), viewMatrix);
		Matrix4f.translate(viewMatrix, position, viewMatrix);
		position.negate();
		viewFrustum.recalculateFrustum(FlounderEngine.getProjectionMatrix(), viewMatrix);
	}

	@Override
	public Matrix4f getViewMatrix() {
		updateViewMatrix();
		return viewMatrix;
	}

	@Override
	public Frustum getViewFrustum() {
		return viewFrustum;
	}

	@Override
	public Matrix4f getReflectionViewMatrix(float planeHeight) {
		throw new NotImplementedException();
	}

	@Override
	public void reflect(float waterHeight) {
		position.y -= 2.0f * (position.y - waterHeight);
		rotation.x = -rotation.x;
		updateViewMatrix();
	}

	@Override
	public Vector3f getPosition() {
		return position;
	}

	@Override
	public Vector3f getRotation() {
		return rotation;
	}

	@Override
	public void setRotation(Vector3f rotation) {
		this.rotation.set(rotation);
	}

	@Override
	public float getAimDistance() {
		throw new NotImplementedException();
	}
}