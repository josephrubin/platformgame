package box.shoe.gameutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

/**
 * Created by Joseph on 10/23/2017.
 */
//fixme: buffering just for screenshot is taking a lot more thread cpu time!
//TODO: when game resumes, should not jump so much from previous frame!
public abstract class AbstractTextureViewScreen extends TextureView implements TextureView.SurfaceTextureListener, Screen
{
    private static final boolean DEBUG_SHOW_BOUNDING_BOXES = false; //TODO: allow to be set from public function
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

        // Clear the canvas
        surfaceCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        paint(surfaceCanvas, gameState);
        // Create debug artifacts, which follow the actual in-game positions, and box each Entity.
        if (AbstractTextureViewScreen.DEBUG_SHOW_BOUNDING_BOXES)
        {
            // We will draw all Entities, not just Paintables.
            for (Entity entity : gameState.interps.keySet())
            {
                surfaceCanvas.drawRect((float) entity.x, (float) entity.y, (float) (entity.x + entity.width), (float) (entity.y + entity.height), new Paint(Paint.ANTI_ALIAS_FLAG));
            }
        }

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

    public Bitmap getScreenshot()
    {
        return null;
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
            unregisterReadyForPaintingListener();
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
            unregisterReadyForPaintingListener();
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
