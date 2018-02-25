package box.shoe.gameutils.screen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.TextureView;
import android.view.View;

import box.shoe.gameutils.engine.GameState;

/**
 * Created by Joseph on 10/23/2017.
 */
//TODO: unfinished. don't use.
public abstract class AbstractTextureViewScreen extends TextureView implements TextureView.SurfaceTextureListener, Screen
{
    private volatile boolean surfaceReady = false;
    private boolean preparedToPaint = false;

    // Canvas returned from lockCanvas. Must be passed back to unlockCanvasAndPost.
    private Canvas surfaceCanvas;

    private boolean hasDimensions = false;
    private Runnable readyForPaintingListener;

    public AbstractTextureViewScreen(Context context, Runnable readyForPaintingListener)
    {
        super(context);
        setSurfaceTextureListener(this);
        this.readyForPaintingListener = readyForPaintingListener;
        // Something that is not documented is that the onDraw/draw methods
        // which are final and cannot be overridden are actually necessary for the surface
        // to be created, therefore we may not call setWillNotDraw(true).
        // This caused me a lot of pain before I knew this would be an issue.
    }

    @Override
    public void preparePaint()
    {
        if (!surfaceReady)
        {
            throw new IllegalStateException("Surface is not ready to paint. Please call canVisualize() to check.");
        }
        surfaceCanvas = lockCanvas();

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
        unlockCanvasAndPost(surfaceCanvas);
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
        readyForPaintingListener = null;
        surfaceCanvas = null;
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

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
    {
        surfaceReady = true;

        if (width > 0 && height > 0)
        {
            hasDimensions = true;
        }

        if (hasDimensions && readyForPaintingListener != null)
        {
            readyForPaintingListener.run();
            clearReadyForPaintingListener();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface)
    {
        cleanup();
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
    {
        if (width > 0 && height > 0)
        {
            hasDimensions = true;
        }

        if (hasDimensions && surfaceReady && readyForPaintingListener != null)
        {
            readyForPaintingListener.run();
            clearReadyForPaintingListener();
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public void setWillNotDraw(boolean willNotDraw)
    {
        throw new UnsupportedOperationException("Can not disable drawing, because that will break the screen.");
    }
}
