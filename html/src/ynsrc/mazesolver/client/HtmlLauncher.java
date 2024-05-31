package ynsrc.mazesolver.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import ynsrc.mazesolver.MazeSolver;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                // Resizable application, uses available space in browser
                // return new GwtApplicationConfiguration(true);
                // Fixed size application:
                return new GwtApplicationConfiguration((int)MazeSolver.SCREEN_WIDTH, (int)MazeSolver.SCREEN_HEIGHT);
        }

        @Override
        public ApplicationListener createApplicationListener () {
                return new MazeSolver();
        }
}