package ynsrc.mazesolver;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import js42721.maze.RecursiveBacktracker;
import js42721.maze.TileMaze;

public class MazeSolver extends Game {
    public static final float PPM = 300f;
    public static final float WORLD_WIDTH = 2.25f;
    public static final float WORLD_HEIGHT = WORLD_WIDTH * (3/4f);
    public static final float SCREEN_WIDTH = WORLD_WIDTH * PPM;
    public static final float SCREEN_HEIGHT = WORLD_HEIGHT * PPM;

    World world;
    Viewport viewport, uiViewport;
    OrthographicCamera camera, uiCamera;
    Box2DDebugRenderer debugRenderer;
    Robot robot;
    TileMaze tileMaze;
    ShapeRenderer shapeRenderer;
    SpriteBatch spriteBatch;

    @Override
    public void create() {
        world = new World(new Vector2(0f, 0f), true);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
        viewport = new FillViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        uiCamera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
        uiViewport = new FillViewport(SCREEN_WIDTH, SCREEN_HEIGHT, uiCamera);

        spriteBatch = new SpriteBatch();

        tileMaze = new TileMaze(new RecursiveBacktracker(32, 32));
        tileMaze.generate();

        float N = 0.25f;

        Vector2 startPos = new Vector2();

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
                } else if (startPos.isZero()) {
                    startPos = new Vector2(x + N, y + N);
                }
            }
        }

        robot = new Robot(this, startPos);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        world.step(1 / 60f, 6, 2);

        robot.act(Gdx.graphics.getDeltaTime());

        camera.position.set(robot.body.getPosition(), 0);
        camera.update();

        debugRenderer.render(world, camera.combined);

        shapeRenderer.setProjectionMatrix(camera.combined);
        robot.drawDebug(shapeRenderer);


        spriteBatch.setProjectionMatrix(uiCamera.projection);
        spriteBatch.begin();
        robot.draw(spriteBatch, 1.0f);
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        shapeRenderer.dispose();
        spriteBatch.dispose();
        robot.dispose();
    }
}
