package box.gift.libprofiler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import box.shoe.gameutils.AbstractSurfaceViewScreen;
import box.shoe.gameutils.GameState;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.SimpleEmitter;

/**
 * Created by Joseph on 2/8/2018.
 */

public class ProfileScreen extends AbstractSurfaceViewScreen
{
    private Paint scorePaint;

    public ProfileScreen(Context context, Runnable readyForPaintingListener)
    {
        super(context, readyForPaintingListener);
    }

    @Override
    public void initialize()
    {
        scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scorePaint.setColor(Color.BLACK);
        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setTextSize((float) (getWidth() / 18));
    }

    @Override //TODO: zoom out when player goes too high?
    protected void paint(Canvas canvas, GameState gameState)
    {
        canvas.drawColor(Color.WHITE);

        List<Paintable> platforms = gameState.get(ProfileEngine.PLATFORMS);
        for (Paintable platform : platforms)
        {
            platform.paint(canvas, getResources());
        }

        List<Paintable> enemies = gameState.get(ProfileEngine.ENEMIES);
        for (Paintable enemy : enemies)
        {
            enemy.paint(canvas, getResources());
        }

        Paintable player = gameState.get(ProfileEngine.PLAYER);
        player.paint(canvas, getResources());

        Paintable attack = gameState.get(ProfileEngine.ATTACK);
        if (attack != null)
        {
            attack.paint(canvas, getResources());
        }

        Paintable landEmitter = gameState.get(ProfileEngine.LAND_EMITTER);
        landEmitter.paint(canvas, getResources());

        // Score.
        int score = gameState.get(ProfileEngine.SCORE);
        Rect bounds = new Rect();
        scorePaint.getTextBounds(String.valueOf(score), 0, 1, bounds);
        int height = bounds.height();
        canvas.drawText(String.valueOf(score), getWidth() / 2, height + getHeight() / 60, scorePaint);
    }
}
