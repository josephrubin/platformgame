package box.gift.libprofiler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Xfermode;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.AccessMode;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import box.shoe.gameutils.Rand;
import box.shoe.gameutils.camera.RectCamera;
import box.shoe.gameutils.debug.Benchmarker;
import box.shoe.gameutils.map.MapLoader;
import box.shoe.gameutils.screen.AbstractSurfaceViewScreen;
import box.shoe.gameutils.engine.GameState;

/**
 * Created by Joseph on 2/8/2018.
 */

public class ProfileScreen extends AbstractSurfaceViewScreen
{ //TODO: Clouds bitmap must disable transparancy so it draws faster.
    //TODO: must be able to recycle the bitmaps on cleanup().
    private RectCamera rectCamera;
    private Paint scorePaint;
    private Rect textMeasureBounds;

    private Paint backgroundPaint;

    // Mountains.
    private Bitmap prescaledMountains;
    private Bitmap mountains;
    private int mountainsWidth;
    private int mountainsHeight;
    private int mountainsX;

    // Clouds.
    private Bitmap prescaledClouds;
    private Bitmap clouds;
    private int cloudsWidth;
    private int cloudsHeight;
    private int cloudsX;

    // Retry.
    private Bitmap retry;
    private Bitmap play;

    public ProfileScreen(Context context, Runnable readyForPaintingListener) //TODO: scaling bitmaps at draw time is a little slower, but loading up a bigger version at load time takes way more space. Solution might be to scale the bitmap at the very end.... (if everything should be scaled up to that point)
    {
        super(context, readyForPaintingListener);
        textMeasureBounds = new Rect();

        // To make pixel art look good, we must disable scale effects...
        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(false);
        backgroundPaint.setFilterBitmap(false);
        // ...and also disable auto-scaling on load-time.
        // An alternative to this step is placing the bitmaps in drawable-nodpi
        // if we are fine with one size for all displays.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        // Mountains.
        mountains = BitmapFactory.decodeResource(getResources(), R.drawable.mountains, options);
        mountainsWidth = mountains.getWidth();
        mountainsHeight = mountains.getHeight();

        // Clouds.
        clouds = BitmapFactory.decodeResource(getResources(), R.drawable.clouds, options);
        cloudsWidth = clouds.getWidth();
        cloudsHeight = clouds.getHeight();

        retry = BitmapFactory.decodeResource(getResources(), R.drawable.retry, options);
        play = BitmapFactory.decodeResource(getResources(), R.drawable.play, options);
    }

    @Override
    public void initialize()
    {
        try
        {
            MapLoader.fromXml(getResources().getAssets(), "map/test.tmx");
        } catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (SAXException e)
        {
            e.printStackTrace();
        }

        scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scorePaint.setColor(Color.BLACK);
        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setTextSize((float) (getWidth() / 10));
        scorePaint.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "font/overlock.ttf"));

        rectCamera = new RectCamera();
        rectCamera.setBounds(0, 0, getWidth(), getHeight());
        rectCamera.setZoomPivot(0, getHeight());

        prescaledClouds = Bitmap.createScaledBitmap(clouds, (3 * getWidth()), (int) (((float) cloudsHeight / cloudsWidth) * (float) (3 * getWidth())), false);
        clouds.recycle();
        clouds = null;

        prescaledMountains = Bitmap.createScaledBitmap(mountains, (3 * getWidth()), (int) (((float) cloudsHeight / cloudsWidth) * (float) (3 * getWidth())), false);
        mountains.recycle();
        mountains = null;

        //Rand random = new Rand();
        mountainsX = 0;//random.intFrom(0, getWidth() - 1);
        cloudsX = 0;//random.intFrom(0, getWidth() - 1);
    }

    @Override
    protected void paint(Canvas canvas, GameState gameState)
    {
        rectCamera.roll(canvas);

        // Background.
        canvas.drawColor(Color.parseColor("#87CEEB"));

        // Mountains.
        int bitmapWidth = 2 * getWidth();
        int bitmapHeight = (int) (((float) mountainsHeight / mountainsWidth) * (float) bitmapWidth);
        canvas.drawBitmap(prescaledMountains, null, new Rect(mountainsX, getHeight() - bitmapHeight, bitmapWidth + mountainsX, getHeight()), backgroundPaint);
        canvas.drawBitmap(prescaledMountains, null, new Rect(mountainsX + bitmapWidth, getHeight() - bitmapHeight, 2 * bitmapWidth + mountainsX, getHeight()), backgroundPaint);
        mountainsX -= 2;
        if (mountainsX + bitmapWidth <= 0)
        {
            mountainsX = 0;
        }

        // Clouds.
        bitmapWidth = 3 * getWidth();
        bitmapHeight = (int) (((float) cloudsHeight / cloudsWidth) * (float) bitmapWidth);
        canvas.drawBitmap(prescaledClouds, cloudsX, 2 * getHeight() / 5 - bitmapHeight / 2, backgroundPaint);
        canvas.drawBitmap(prescaledClouds, cloudsX + bitmapWidth, 2 * getHeight() / 5 - bitmapHeight / 2, backgroundPaint);
        cloudsX -= 1;
        if (cloudsX + bitmapWidth <= 0)
        {
            cloudsX = 0;
        }

        // Platforms.
        List<Platform> platforms = gameState.get(ProfileEngine.PLATFORMS);
        for (Platform platform : platforms)
        {
            platform.paint(canvas, getResources());
        }

        // Player.
        Player player = gameState.get(ProfileEngine.PLAYER);
        player.paint(canvas, getResources());

        // Score.
        int score = gameState.get(ProfileEngine.SCORE);
        scorePaint.getTextBounds(String.valueOf(score), 0, 1, textMeasureBounds);
        int height = textMeasureBounds.height();
        canvas.drawText(String.valueOf(score), getWidth() / 2, height + getHeight() / 56, scorePaint);

        // Try again button.
        TryAgainButton tryAgainButton = gameState.get(ProfileEngine.TRY_AGAIN_BUTTON);
        if (tryAgainButton != null)
        {
            tryAgainButton.paint(canvas, retry);
        }

        // Play button.
        PlayButton playButton = gameState.get(ProfileEngine.PLAY_BUTTON);
        if (playButton != null)
        {
            playButton.paint(canvas, play);
        }
    }
}
