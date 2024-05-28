package ynsrc.mazesolver;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import js42721.maze.RecursiveBacktracker;
import js42721.maze.TileMaze;

public class MazeSolver extends ApplicationAdapter {
    World world;
    Viewport viewport;
    Camera camera;
    Stage stage;
    Box2DDebugRenderer debugRenderer;
    ShapeRenderer shapeRenderer;
    Robot robot;

    TileMaze tileMaze;

    @Override
    public void create() {
        world = new World(new Vector2(0f, 0f), true);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera(30, 30);
        viewport = new FillViewport(30f / 2f, 30f / 2f, camera);
        stage = new Stage(viewport);

        RecursiveBacktracker recursiveBacktracker = new RecursiveBacktracker(30, 30);

        tileMaze = new TileMaze(recursiveBacktracker);
        tileMaze.generate();

        int N = 2;

        Vector2 startPos = Vector2.Zero;

        for (int y = 0; y < tileMaze.getHeight(); ++y) {
            for (int x = 0; x < tileMaze.getWidth(); ++x) {
                if (tileMaze.isWall(x, y)) {
                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    bodyDef.position.set(x * N, y * N);
                    Body body = world.createBody(bodyDef);

                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(N / 2f, N / 2f);
                    body.createFixture(shape, 1.0f);
                } else {
                    if (startPos == Vector2.Zero) {
                        startPos = new Vector2(x + 1, y + 1);
                    }
                }
            }
        }



        robot = new Robot(world, startPos);
        robot.debug();

        stage.addActor(robot);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        world.step(1 / 60f, 6, 2);

        stage.act(Gdx.graphics.getDeltaTime());

        camera.position.set(robot.body.getPosition(), 0);
        camera.update();

        stage.draw();

        debugRenderer.render(world, camera.combined);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        world.dispose();
    }
}
