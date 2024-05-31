package ynsrc.mazesolver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;

/**
 * This class controls and draws the maze solver robot.
 */
public class Robot extends Actor implements Disposable {
    /** Left and Right ray lengths in meters. */
    private static final float SIDE_RAY_LENGTH = 0.2f;

    /** Front ray length in meters. */
    private static final float FRONT_RAY_LENGTH = 0.2f;

    /** Radius of robot body in meters. */
    private final float BODY_RADIUS = 0.05f;

    /** Force to apply DC motors in Newton [N] to move or rotate robot. */
    private final Vector2 MOTOR_FORCE = new Vector2(10e-3f, 10e-3f);

    /** Caller game class of maze solver simulation for accessing to viewport and other members. */
    private final MazeSolver mazeSolver;

    /** Box2D world object of maze solver simulation. */
    private final World world;

    /** Robot body in Box2D world. */
    Body body;

    /** Center point of the robot in the world. */
    private Vector2 robotCenter;

    /** Ray for front sensor. */
    private final Ray2D rayFront = new Ray2D();

    /** Ray for left sensor. */
    private final Ray2D rayLeft = new Ray2D();

    /** Ray for right sensor. */
    private final Ray2D rayRight = new Ray2D();

    /** Collisions points for sensor rays. */
    private Vector2 collisionFront, collisionLeft, collisionRight;

    /** Distances from sensors in meters. */
    private float distanceFront, distanceLeft, distanceRight;

    /** Sensor detection statuses, true means object detected in range. */
    private boolean sensorFront, sensorLeft, sensorRight;

    /** Motor object for DC motor simulation. */
    private final Motor leftMotor = new Motor(), rightMotor = new Motor();

    /** Font for drawing texts on screen. */
    private final BitmapFont font;

    /**
     * Main constructor of maze solver robot actor.
     * @param mazeSolver caller game (simulation) object.
     * @param startPos initial position of the robot in world.
     */
    public Robot(MazeSolver mazeSolver, Vector2 startPos) {
        this.mazeSolver = mazeSolver;
        world = mazeSolver.world;
        font = new BitmapFont();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startPos.x, startPos.y);

        body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(BODY_RADIUS);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 10f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.4f;

        body.createFixture(fixtureDef);

        circle.dispose();
    }

    /** Sets the motors to move forward.  */
    private void moveForward() {
        leftMotor.setSpeed(1.0f);
        rightMotor.setSpeed(1.0f);
        leftMotor.direction = MotorDirection.FORWARD;
        rightMotor.direction = MotorDirection.FORWARD;
    }

    /* Sets the motors to move forward and left. */
    private void moveForwardLeft() {
        leftMotor.setSpeed(0.85f);
        rightMotor.setSpeed(1.0f);
        leftMotor.direction = MotorDirection.FORWARD;
        rightMotor.direction = MotorDirection.FORWARD;
    }

    /* Sets the motors to move forward and right. */
    private void moveForwardRight() {
        leftMotor.setSpeed(1.0f);
        rightMotor.setSpeed(0.85f);
        leftMotor.direction = MotorDirection.FORWARD;
        rightMotor.direction = MotorDirection.FORWARD;
    }

    /** Sets the motors to turn left.  */
    private void turnLeft() {
        leftMotor.setSpeed(0.25f);
        rightMotor.setSpeed(0.25f);
        leftMotor.direction = MotorDirection.REVERSE;
        rightMotor.direction = MotorDirection.FORWARD;
    }

    /** Sets the motors to turn right. */
    private void turnRight() {
        leftMotor.setSpeed(0.25f);
        rightMotor.setSpeed(0.25f);
        leftMotor.direction = MotorDirection.FORWARD;
        rightMotor.direction = MotorDirection.REVERSE;
    }

    /** Sets the motors to idle. */
    private void stop() {
        leftMotor.direction = MotorDirection.NONE;
        rightMotor.direction = MotorDirection.NONE;
    }

    /** Simulate DC motors to move or rotate the robot. */
    private void simulateMotors() {
        Vector2 forwardDir = rayFront.end.cpy().sub(rayFront.start).nor();

        if (leftMotor.direction != MotorDirection.NONE) {
            float deg = leftMotor.direction == MotorDirection.FORWARD ? 0 : 180;
            body.applyForce(MOTOR_FORCE.cpy().scl(forwardDir).rotateDeg(deg).scl(leftMotor.getSpeed()), rayLeft.start, true);
        }

        if (rightMotor.direction != MotorDirection.NONE) {
            float deg = rightMotor.direction == MotorDirection.FORWARD ? 0 : 180;
            body.applyForce(MOTOR_FORCE.cpy().scl(forwardDir).rotateDeg(deg).scl(rightMotor.getSpeed()), rayRight.start, true);
        }
    }

    /** Maze solving algorithm for the robot. */
    private void solveMaze() {
        if (distanceFront < 0.2f) {
            // front sensor detected wall in 20 cm
            if (distanceRight < 0.2f) {
                // right sensor also detected wall in 20 cm
                turnLeft();
            } else {
                // right sensor not detected any wall
                turnRight();
            }
        } else {
            // front sensor not detected any wall
            if (distanceRight > 0.15f) {
                // right sensor also not detected any wall in 15 cm
                turnRight();
            } else  {
                // right sensor detected wall in 15 cm
                if (distanceRight < 0.06f) {
                    // right sensor detected wall in 6 cm
                    moveForwardLeft();
                } else if (distanceLeft < 0.06f) {
                    // left sensor detected wall in 6 cm
                    moveForwardRight();
                } else  {
                    moveForward();
                }
            }
        }
    }

    @Override
    public void act(float delta) {
        body.setLinearVelocity(body.getLinearVelocity().scl(0.98f));
        body.setAngularVelocity(body.getAngularVelocity() * 0.98f);

        float forwardAngle = body.getAngle();
        float leftAngle = forwardAngle + MathUtils.PI / 3;
        float rightAngle = forwardAngle - MathUtils.PI / 3;

        robotCenter = body.getWorldCenter();

        rayFront.start = robotCenter.cpy().add(BODY_RADIUS * MathUtils.cos(forwardAngle), BODY_RADIUS * MathUtils.sin(forwardAngle));
        rayFront.end = rayFront.start.cpy().add(MathUtils.cos(forwardAngle) * FRONT_RAY_LENGTH,MathUtils.sin(forwardAngle) * FRONT_RAY_LENGTH);

        rayLeft.start = robotCenter.cpy().add(BODY_RADIUS * MathUtils.cos(leftAngle), BODY_RADIUS * MathUtils.sin(leftAngle));
        rayLeft.end = rayLeft.start.cpy().add(MathUtils.cos(leftAngle) * SIDE_RAY_LENGTH, MathUtils.sin(leftAngle) * SIDE_RAY_LENGTH);

        rayRight.start = robotCenter.cpy().add(BODY_RADIUS * MathUtils.cos(rightAngle), BODY_RADIUS * MathUtils.sin(rightAngle));
        rayRight.end = rayRight.start.cpy().add(MathUtils.cos(rightAngle) * SIDE_RAY_LENGTH, MathUtils.sin(rightAngle) * SIDE_RAY_LENGTH);

        sensorFront = false;
        sensorLeft = false;
        sensorRight = false;

        distanceFront = 1000.0f;
        distanceLeft = 1000.0f;
        distanceRight = 1000.0f;

        collisionFront = robotCenter.cpy();
        collisionLeft = robotCenter.cpy();
        collisionRight = robotCenter.cpy();

        world.rayCast((fixture, point, normal, fraction) -> {
            sensorFront = true;
            distanceFront = point.dst(rayFront.start);
            collisionFront = point.cpy();
            return 1;
        }, rayFront.start, rayFront.end);

        world.rayCast((fixture, point, normal, fraction) -> {
            sensorLeft = true;
            distanceLeft = point.dst(rayLeft.start);
            collisionLeft = point.cpy();
            return 1;
        }, rayLeft.start, rayLeft.end);

        world.rayCast((fixture, point, normal, fraction) -> {
            sensorRight = true;
            distanceRight = point.dst(rayRight.start);
            collisionRight = point.cpy();
            return 1;
        }, rayRight.start, rayRight.end);

        simulateMotors();

        solveMaze();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveForward();
        if (Gdx.input.isKeyPressed(Input.Keys.A)) turnLeft();
        if (Gdx.input.isKeyPressed(Input.Keys.S)) stop();
        if (Gdx.input.isKeyPressed(Input.Keys.D)) turnRight();
    }

    /** Used to support GWT/HTML5 build which does not support String.format(...) method. */
    private String floatFormat(float number) {
        String val = String.valueOf(number).replace(",", ".");
        String[] raw = val.split("\\.");
        return raw[0] + "." + raw[1].substring(0, Math.min(raw[1].length(), 2));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float xRatio = Gdx.graphics.getWidth() / MazeSolver.SCREEN_WIDTH;
        float yRatio =  Gdx.graphics.getHeight() / MazeSolver.SCREEN_HEIGHT;

        if (sensorFront) {
            Vector2 screenCoordinates = mazeSolver.viewport.project(rayFront.end.cpy().scl(1.01f));
            font.draw(
                    batch,
                    floatFormat(distanceFront * 100f) + " cm",
                    screenCoordinates.x / xRatio,
                    screenCoordinates.y / yRatio
            );
        }

        if (sensorLeft) {
            Vector2 screenCoordinates = mazeSolver.viewport.project(rayLeft.end.cpy().scl(1.01f));
            font.draw(
                    batch,
                    floatFormat(distanceLeft * 100f) + " cm",
                    screenCoordinates.x / xRatio,
                    screenCoordinates.y / yRatio
            );
        }

        if (sensorRight) {
            Vector2 screenCoordinates = mazeSolver.viewport.project(rayRight.end.cpy().scl(1.01f));
            font.draw(
                    batch,
                    floatFormat(distanceRight * 100f) + " cm",
                    screenCoordinates.x / xRatio,
                    screenCoordinates.y / yRatio
            );
        }
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        shapes.begin(ShapeRenderer.ShapeType.Line);

        shapes.setColor(sensorFront ? Color.RED : Color.GREEN);
        shapes.line(rayFront.start, rayFront.end);

        shapes.setColor(sensorLeft ? Color.RED : Color.GREEN);
        shapes.line(rayLeft.start, rayLeft.end);

        shapes.setColor(sensorRight ? Color.RED : Color.GREEN);
        shapes.line(rayRight.start, rayRight.end);

        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.RED);

        if (sensorFront) {
            shapes.rect(collisionFront.x - 0.01f, collisionFront.y - 0.01f, 0.02f, 0.02f);
        }

        if (sensorLeft) {
            shapes.rect(collisionLeft.x - 0.01f, collisionLeft.y - 0.01f, 0.02f, 0.02f);
        }

        if (sensorRight) {
            shapes.rect(collisionRight.x - 0.01f, collisionRight.y - 0.01f, 0.02f, 0.02f);
        }

        shapes.end();
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
