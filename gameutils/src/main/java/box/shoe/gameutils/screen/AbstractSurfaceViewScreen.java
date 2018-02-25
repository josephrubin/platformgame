package box.shoe.gameutils.screen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import box.shoe.gameutils.engine.GameState;

/**
 * Created by Joseph on 10/23/2017.
 */
//TODO: when game resumes, should not jump so much from previous frame! (engine problem).
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
        holder.setFormat(PixelFormat.TRANSPARENT);
        setClickable(true);
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
        paint(surfaceCanvas, gameState);
        postCanvas();
    }

    @Override
    public void clearScreen()
    {
        checkState();
        surfaceCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        postCanvas();
    }

    protected abstract void paint(Canvas canvas, GameState gameState);

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        this.holder = holder;
        surfaceReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if (width > 0 && height > 0)
        {
            hasDimensions = true;
            synchronized (this)
            {
                if (readyForPaintingListener != null)
                {
                    this.readyForPaintingListener.run();
                    clearReadyForPaintingListener();
                }
            }
        }
    }

    public void setReadyForPaintingListener(Runnable readyForPaintingListener)
    {
        this.readyForPaintingListener = readyForPaintingListener;
        synchronized (this)
        {
            if (hasDimensions && surfaceReady && readyForPaintingListener != null)
            {
                this.readyForPaintingListener.run();
                clearReadyForPaintingListener();
            }
        }
    }

    @Override
    public View asView()
    {
        return this;
    }

    public void clearReadyForPaintingListener() //Irreversable
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
        clearReadyForPaintingListener();
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
}
