package box.shoe.gameutils.screen;

import android.view.View;

import box.shoe.gameutils.engine.GameState;

/**
 * Created by Joseph on 1/1/2018.
 * An object which can display a game to the Android user.
 */

public interface Screen
{
    // Called before drawing begins, once the game, and thus the screen, has dimensions.
    void initialize();
    boolean hasInitialized();

    void preparePaint();
    void clearScreen();
    boolean hasPreparedPaint();

    void paintFrame(GameState gameState);

    void cleanup();

    int getWidth();
    int getHeight();

    View asView();

    void setOnTouchListener(View.OnTouchListener onTouchListener);

    void setReadyForPaintingListener(Runnable readyForPaintingListener);
    void clearReadyForPaintingListener();
}
