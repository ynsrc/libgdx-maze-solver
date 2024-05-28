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

    private Vector2 robotCenter, forwardRay, leftRay, rightRay;
    private float rayLength = 2f;

    public Robot(World world, Vector2 startPos) {
        this.world = world;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startPos.x, startPos.y);

        body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(0.4f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.1f;

        body.createFixture(fixtureDef);

        circle.dispose();
    }

    @Override
    public void act(float delta) {
        float forwardAngle = body.getAngle();
        float leftAngle = forwardAngle - MathUtils.PI / 2;
        float rightAngle = forwardAngle + MathUtils.PI / 2;

        body.setLinearVelocity(body.getLinearVelocity().scl(0.98f));
        body.setAngularVelocity(body.getAngularVelocity() * 0.97f);

        robotCenter = body.getWorldCenter();

        forwardRay = robotCenter.cpy().add(MathUtils.cos(forwardAngle) * rayLength, MathUtils.sin(forwardAngle) * rayLength);
        leftRay = robotCenter.cpy().add(MathUtils.cos(leftAngle) * rayLength, MathUtils.sin(leftAngle) * rayLength);
        rightRay = robotCenter.cpy().add(MathUtils.cos(rightAngle) * rayLength, MathUtils.sin(rightAngle) * rayLength);

        world.rayCast((fixture, point, normal, fraction) -> {

            return 1;
        }, robotCenter, forwardRay);

        world.rayCast((fixture, point, normal, fraction) -> {

            return 1;
        }, robotCenter, leftRay);

        world.rayCast((fixture, point, normal, fraction) -> {

            return 1;
        }, robotCenter, rightRay);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            Vector2 forward = new Vector2(MathUtils.cos(forwardAngle), MathUtils.sin(forwardAngle)).scl(0.01f);
            body.applyLinearImpulse(forward, robotCenter, true);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            body.applyTorque(0.05f, true);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            body.applyTorque(-0.05f, true);
        }
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        shapes.setProjectionMatrix(getStage().getCamera().combined);

        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.line(robotCenter, forwardRay);
        shapes.setColor(Color.RED);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.line(robotCenter, leftRay);
        shapes.setColor(Color.YELLOW);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.line(robotCenter, rightRay);
        shapes.setColor(Color.BLUE);
        shapes.end();
    }
}
