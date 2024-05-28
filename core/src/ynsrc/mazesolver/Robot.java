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

    private World world;
    Body body;
    private float bodyRadius = 0.4f;

    private Vector2 robotCenter, forwardRay, leftRay, rightRay;
    private float rayLength = 2f;

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

    @Override
    public void act(float delta) {
        float forwardAngle = body.getAngle();
        float leftAngle = forwardAngle + MathUtils.PI / 2;
        float rightAngle = forwardAngle - MathUtils.PI / 2;

        body.setLinearVelocity(body.getLinearVelocity().scl(0.98f));
        body.setAngularVelocity(body.getAngularVelocity() * 0.98f);

        robotCenter = body.getWorldCenter();

        forwardRay = robotCenter.cpy().add(MathUtils.cos(forwardAngle) * rayLength, MathUtils.sin(forwardAngle) * rayLength);
        leftRay = robotCenter.cpy().add(MathUtils.cos(leftAngle) * rayLength, MathUtils.sin(leftAngle) * rayLength);
        rightRay = robotCenter.cpy().add(MathUtils.cos(rightAngle) * rayLength, MathUtils.sin(rightAngle) * rayLength);

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

        if (delay > 0) {
            delay -= delta;
            return;
        }

        if (sensorFront) {
            if (!sensorRight) {
                turnRight();
                delay = 2.0f;
            } else {
                turnLeft();
            }
        } else {
            moveForward();
        }

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
