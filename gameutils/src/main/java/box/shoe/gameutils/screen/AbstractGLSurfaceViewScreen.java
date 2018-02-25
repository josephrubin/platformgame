package box.shoe.gameutils.screen;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import box.shoe.gameutils.engine.GameState;

/**
 * Created by Joseph on 1/31/2018.
 */
//TODO: unfinished, untested. don't use. it might even be removed
public abstract class AbstractGLSurfaceViewScreen extends GLSurfaceView implements Screen, GLSurfaceView.Renderer
{
    private volatile boolean surfaceReady = false;
    private boolean hasDimensions = false;
    private boolean preparedToPaint = false;

    private Runnable readyForPaintingListener;
    private GameState gameStateToPaint;

    public AbstractGLSurfaceViewScreen(Context context, Runnable readyForPaintingListener)
    {
        super(context);
        this.readyForPaintingListener = readyForPaintingListener;
        setEGLContextClientVersion(2);
        setRenderer(this); //TODO: is this necessary? or will this auto get the callbacks?
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        System.out.println("GL surface created");
        surfaceReady = true;
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        System.out.println("GL surface changed");
        GLES20.glViewport(0, 0, width, height);
        if (width > 0 && height > 0)
        {
            hasDimensions = true;
            if (readyForPaintingListener != null)
            {
                readyForPaintingListener.run();
                clearReadyForPaintingListener();
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 unused)
    {
        paint(gameStateToPaint);
    }

    protected abstract void paint(GameState gameState);

    @Override
    public boolean hasInitialized()
    {
        return surfaceReady && hasDimensions;
    }

    @Override
    public void preparePaint()
    {
        if (!surfaceReady)
        {
            throw new IllegalStateException("Surface is not ready to paint. Please call canVisualize() to check.");
        }

        preparedToPaint = true;
    }

    @Override
    public void clearScreen()
    {

    }

    @Override
    public boolean hasPreparedPaint()
    {
        return preparedToPaint;
    }

    @Override
    public final void paintFrame(@NotNull GameState gameState)
    {
        checkState();

        this.gameStateToPaint = gameState;

        requestRender();

        // Create debug artifacts, which follow the actual in-game positions, and box each Entity.

        preparedToPaint = false;
    }

    @Override
    public void cleanup()
    {

    }

    @Override
    public View asView()
    {
        return this;
    }

    @Override
    public void clearReadyForPaintingListener()
    {
        readyForPaintingListener = null;
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
}
