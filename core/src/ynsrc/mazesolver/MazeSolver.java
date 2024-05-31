package ynsrc.mazesolver;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import js42721.maze.RecursiveBacktracker;
import js42721.maze.TileMaze;

/**
 * Main class for Maze Solver simulation.
 * You can visit <a href="https://github.com/ynsrc/libgdx-maze-solver">Github Repo</a>
 * @author YNSRC
 */
public class MazeSolver extends Game implements InputProcessor {
    /** Pixels per meter. 1 meter in Box2D shown in 300 pixels */
    public static final float PPM = 300f;

    /** World width for Box2D world (2.25 meters) */
    public static final float WORLD_WIDTH = 2.25f;

    /** World height for Box2D world (4x3 ratio for WORLD_WIDTH) */
    public static final float WORLD_HEIGHT = WORLD_WIDTH * (3/4f);

    /** Calculated screen width */
    public static final float SCREEN_WIDTH = WORLD_WIDTH * PPM;

    /** Calculated screen height */
    public static final float SCREEN_HEIGHT = WORLD_HEIGHT * PPM;

    /** Box2D World for 2D physics simulation */
    World world;

    /** Viewport scales game for different screen sizes. */
    Viewport viewport;

    /** This camera will follow robot in world. */
    OrthographicCamera camera, uiCamera;

    /** Box2D's debug renderer shows fixtures of bodies in world. */
    Box2DDebugRenderer debugRenderer;

    /** Robot actor which solves the maze. */
    Robot robot;

    /** Shape renderer renders sensor related shapes rays, collision points etc. */
    ShapeRenderer shapeRenderer;

    /** Sprite batch is using for drawing texts from the robot object.  */
    SpriteBatch spriteBatch;

    /**
     * Generates maze thanks to js42721's work and returns start position.
     * @return start position of robot.
     */
    private Vector2 generateMaze() {
        Vector2 startPos = new Vector2();

        TileMaze tileMaze = new TileMaze(new RecursiveBacktracker(32, 32));
        tileMaze.generate();

        float N = 0.25f; // grid size of the maze in meters

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

        return startPos;
    }

    @Override
    public void create() {
        world = new World(new Vector2(0f, 0f), true);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
        viewport = new FillViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        uiCamera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);

        spriteBatch = new SpriteBatch();

        Vector2 startPos = generateMaze();
        robot = new Robot(this, startPos);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        world.step(1 / 60f, 6, 2);

        robot.act(Gdx.graphics.getDeltaTime());

        camera.position.set(robot.body.getPosition(), 0);

        if (Gdx.input.isButtonPressed(Input.Buttons.FORWARD)) {
            camera.zoom += 0.05f;
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.BACK)) {
            camera.zoom -= 0.05f;
        }

        camera.update();

        debugRenderer.render(world, camera.combined);

        shapeRenderer.setProjectionMatrix(camera.combined);
        robot.drawDebug(shapeRenderer);

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

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (amountY != 0) {
            camera.zoom = MathUtils.clamp(camera.zoom + ((amountY > 0) ? 0.1f : -0.1f), 1.0f, 5.0f);
            return true;
        }
        return false;
    }
}
