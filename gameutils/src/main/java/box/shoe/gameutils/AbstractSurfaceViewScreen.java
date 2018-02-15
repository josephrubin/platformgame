package box.shoe.gameutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Joseph on 10/23/2017.
 */
//fixme: buffering just for screenshot is taking a lot more thread cpu time!
    //TODO: when game resumes, should not jump so much from previous frame!
public abstract class AbstractSurfaceViewScreen extends SurfaceView implements SurfaceHolder.Callback, Screen
{
    private SurfaceHolder holder;
    private volatile boolean surfaceReady = false;
    private boolean preparedToPaint = false;

    // Canvas returned from lockCanvas. Must be passed back to unlockCanvasAndPost.
    private Canvas surfaceCanvas;

    private boolean hasDimensions = false;
    private Runnable readyForPaintingListener;

    public AbstractSurfaceViewScreen(Context context, Runnable readyForPaintingListener)
    {
        super(context);
        setWillNotDraw(true);
        this.readyForPaintingListener = readyForPaintingListener;
        holder = getHolder();
        holder.addCallback(this);
    }
/*
    public void giveDataReference(AbstractEngine abstractData)
    {
        this.abstractData = abstractData;
    }
*/

    @Override
    public void preparePaint()
    {
        if (!surfaceReady)
        {
            throw new IllegalStateException("Surface is not ready to paint. Please call canVisualize() to check.");
        }
        surfaceCanvas = holder.lockCanvas();

        // Set coordinate origin to (0, 0) and make +x = right, +y = up.
        /*surfaceCanvas.translate(0, bufferCanvas.getHeight());
        surfaceCanvas.scale(1, -1);*/

        preparedToPaint = true;
    }

    private void checkState()
    {
        if (!hasPreparedPaint())
        {
            throw new IllegalStateException("Not prepared to paintFrame. Please call preparePaint() before calling paintFrame each time.");
        }
        if (!surfaceReady)
        {
            throw new IllegalStateException("Surface is not ready to paint. Please call canVisualize() to check.");
        }
    }

    private void postCanvas()
    {
        preparedToPaint = false;
        holder.unlockCanvasAndPost(surfaceCanvas);
    }

    @Override
    public final void paintFrame(@NonNull GameState gameState)
    {
        checkState();

        // Clear the canvas
        surfaceCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        paint(surfaceCanvas, gameState);

        postCanvas();
    }

    @Override
    public void paintStatic(@NonNull Bitmap bitmap)
    {
        checkState();

        // Clear the canvas
        surfaceCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Draw the bitmap
        surfaceCanvas.drawBitmap(bitmap, 0, 0,null);

        postCanvas();
    }

    @Override
    public void paintStatic(int color)
    {
        checkState();

        // Draw the color
        surfaceCanvas.drawColor(color);

        postCanvas();
    }

    @Override
    public void unpreparePaint()
    {
        checkState();
        surfaceCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        postCanvas();
    }

    protected abstract void paint(Canvas canvas, GameState gameState);

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        surfaceReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if (width > 0 && height > 0)
        {
            hasDimensions = true;
            if (readyForPaintingListener != null)
            {
                readyForPaintingListener.run();
                unregisterReadyForPaintingListener();
            }
        }
    }

    @Override
    public View asView()
    {
        return this;
    }

    public void unregisterReadyForPaintingListener() //Irreversable
    {
        readyForPaintingListener = null;
    }

    public void cleanup()
    {
        if (hasPreparedPaint())
        {
            throw new IllegalStateException("Surface is being cleaned up but we have not yet released the canvas lock! A method must be called to unprepare!");
        }
        surfaceReady = false;
        this.holder = null;
        readyForPaintingListener = null;
        surfaceCanvas = null;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) //fixme: make the cleanup run even if this callback is not
    {
        cleanup();
    }

    @Override
    public boolean hasInitialized()
    {
        return surfaceReady && hasDimensions;
    }

    @Override
    public boolean hasPreparedPaint()
    {
        return preparedToPaint;
    }

    public Bitmap getScreenshot()
    {
        return null;
    }
}
