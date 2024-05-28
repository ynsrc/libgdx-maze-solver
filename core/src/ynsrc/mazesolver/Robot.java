package ynsrc.mazesolver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Robot extends Actor {

    Body body;
    private final World world;
    private final float bodyRadius = 0.4f;

    private Vector2 robotCenter, forwardRay, leftRay, rightRay;

    private boolean sensorFront, sensorLeft, sensorRight;
    private MotorDirection leftMotor = MotorDirection.NONE, rightMotor = MotorDirection.NONE;
    private float delay = 0;

    public Robot(World world, Vector2 startPos) {
        this.world = world;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startPos.x, startPos.y);

        body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(bodyRadius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.1f;

        body.createFixture(fixtureDef);

        circle.dispose();
    }

    private void moveForward() {
        leftMotor = MotorDirection.FORWARD;
        rightMotor = MotorDirection.FORWARD;
    }

    private void turnLeft() {
        leftMotor = MotorDirection.REVERSE;
        rightMotor = MotorDirection.FORWARD;
    }

    private void turnRight() {
        leftMotor = MotorDirection.FORWARD;
        rightMotor = MotorDirection.REVERSE;
    }

    private void stop() {
        leftMotor = MotorDirection.NONE;
        rightMotor = MotorDirection.NONE;
    }

    private void simulateMotors() {
        if (leftMotor == MotorDirection.FORWARD && rightMotor == MotorDirection.FORWARD) {
            float angle = body.getAngle();
            Vector2 forward = new Vector2(MathUtils.cos(angle), MathUtils.sin(angle)).scl(0.01f);
            body.applyLinearImpulse(forward, robotCenter, true);
        }

        if (leftMotor == MotorDirection.REVERSE && rightMotor == MotorDirection.FORWARD) {
            body.applyTorque(0.05f, true);
        }

        if (leftMotor == MotorDirection.FORWARD && rightMotor == MotorDirection.REVERSE) {
            body.applyTorque(-0.05f, true);
        }
    }

    private void solveMaze(float delta) {
        if (delay > 0) {
            delay -= delta;
            return;
        }

        if (sensorFront) {
            if (sensorRight) {
                turnLeft();
            } else {
                turnRight();
            }
        } else {
            if (!sensorRight) {
                turnRight();
            } else  {
                moveForward();
            }
        }
    }

    @Override
    public void act(float delta) {
        float forwardAngle = body.getAngle();
        float leftAngle = forwardAngle + MathUtils.PI / 3;
        float rightAngle = forwardAngle - MathUtils.PI / 3;

        body.setLinearVelocity(body.getLinearVelocity().scl(0.98f));
        body.setAngularVelocity(body.getAngularVelocity() * 0.98f);

        robotCenter = body.getWorldCenter();

        float sideRayLength = 2f;
        float frontRayLength = 2f;

        forwardRay = robotCenter.cpy().add(MathUtils.cos(forwardAngle) * frontRayLength, MathUtils.sin(forwardAngle) * frontRayLength);
        leftRay = robotCenter.cpy().add(MathUtils.cos(leftAngle) * sideRayLength, MathUtils.sin(leftAngle) * sideRayLength);
        rightRay = robotCenter.cpy().add(MathUtils.cos(rightAngle) * sideRayLength, MathUtils.sin(rightAngle) * sideRayLength);

        sensorFront = false;
        sensorLeft = false;
        sensorRight = false;

        world.rayCast((fixture, point, normal, fraction) -> {
            sensorFront = true;
            return 1;
        }, robotCenter, forwardRay);

        world.rayCast((fixture, point, normal, fraction) -> {
            sensorLeft = true;
            return 1;
        }, robotCenter, leftRay);

        world.rayCast((fixture, point, normal, fraction) -> {
            sensorRight = true;
            return 1;
        }, robotCenter, rightRay);

        simulateMotors();

        solveMaze(delta);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveForward();
        if (Gdx.input.isKeyPressed(Input.Keys.A)) turnLeft();
        if (Gdx.input.isKeyPressed(Input.Keys.D)) turnRight();
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(sensorFront ? Color.RED : Color.GREEN);
        shapes.line(robotCenter, forwardRay);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(sensorLeft ? Color.RED : Color.GREEN);
        shapes.line(robotCenter, leftRay);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(sensorRight ? Color.RED : Color.GREEN);
        shapes.line(robotCenter, rightRay);
        shapes.end();
    }
}
