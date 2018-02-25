package box.shoe.gameutils.engine;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.RestrictTo;
import android.util.Log;
import android.util.LogPrinter;
import android.view.Choreographer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import box.gift.gameutils.R;
import box.shoe.gameutils.debug.Benchmarker;
import box.shoe.gameutils.debug.L;
import box.shoe.gameutils.screen.Screen;

/**
 * Created by Joseph on 10/23/2017.
 *
 * The Game Engine uses two threads. One for game updates, and one to paint frames.
 * Game updates are run at a frequency determined by the supplied UPS given in the constructor.
 * Frames are painted at each new VSYNC (Screen refresh), supplied by internal Choreographer.
 * Because these are not aligned, the engine interpolates between two game states for frame painting based on time,
 * and gives the interpolated game state to the Screen supplied to the constructor.
 */ //TODO: standardize how random instances are used.
public abstract class AbstractEngine //TODO: redo input system. make it easy, usable.
{ //TODO: remove isActive()/isPlaying() and replace with a single state variable.
    //TODO: need an easier way to load scaled bitmaps. use preload scaling if possible (pow of 2), and after loading scaling otherwise. Have pixel art mode to disable filtering/anti-aliasing.
    // Define the possible UPS options, which are factors of 1000 (so we get an even number of MS per update).
    // This is not a hard requirement, and the annotation may be suppressed,
    // at the risk of possible jittery frame display. //TODO: is this actually a real concern?
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({1, 2, 4, 5, 8, 10, 20, 25, 40, 50, 100, 125, 200, 250, 500, 1000})
    public @interface UPS_Options {}

    // Number of Updates Per Second that we would like to receive.
    // There are timing accuracy limitations,
    // and it is possible for the updates to take too long
    // for this to be possible (lag), hence 'target.'
    private final int targetUPS;

    // Based on the targetUPS we can define how long we expect each update to take.
    private final long expectedUpdateTimeMS;
    private final long expectedUpdateTimeNS;

    // The screen which will display a representation of the state of the game.
    private Screen gameScreen;

    // Control - try to mitigate use of volatile variables when possible.
    // (issue is not the number of volatile variables, rather how often they
    // are read causing memory to be flushed by the thread that set it).
    // Be careful with ints, because even incrementation is three operations (not wholly atomic).
    private volatile boolean started = false;
    private volatile boolean stopped = false;
    private volatile boolean stopThreads = false;
    private volatile boolean paused = false;
    private volatile boolean pauseThreads = false;

    // Threads.
    // Runs game updates.
    private Thread updateThread;
    private Looper updateThreadLooper;
    // Runs frame rendering.
    private Thread frameThread;
    private Looper frameThreadLooper;
    // For instantiating CountDownLatches.
    private final int NUMBER_OF_THREADS = 2;

    // Concurrent - for simplicity, define as few as possible.
    // Always used in no more than one un-synchronized blocks, so no need to be volatile?
    private final Object monitorUpdateFrame = new Object();
    private final Object monitorControl = new Object();
    private CountDownLatch pauseLatch; // Makes sure all necessary threads pause before returning pauseGame.
    private CountDownLatch stopLatch; // Makes sure all necessary threads stop before returning stopGame.

    // Objs - remember to cleanup those that can be!
    private Choreographer vsync;
    private List<GameState> gameStates;

    // Const //TODO: remove?
    public static final int INACTIVE = 0;
    public static final int PLAYING = 1;
    public static final int PAUSED = 2;

    // Etc. //TODO: sort these
    protected volatile boolean screenTouched = false;

    // Fixed display mode - display will attempt to paint
    // pairs of updates for a fixed amount of time (expectedUpdateDelayNS)
    // regardless of the amount of time that passed between
    // the generation of the two updates. When an update
    // happens too quickly or slowly, this will cut short
    // or artificially lengthen the painting of a pair of updates
    // because the next update comes too early or late.
    // Pro: looks better when an occasional update comes too early or too late.
    private static final int DIS_MODE_FIX_UPDATE_DISPLAY_DURATION = 0;

    // Varied display mode - display will lengthen or shorten
    // the amount of time it takes to display a pair of updates.
    // When an update happens too quickly or slowly, this will
    // cause quite a jitter.
    // Pro: When many updates in a row come to early or late, instead
    // of jittering the display will simply speed up or slow down
    // to keep pace with the updates, which looks very nice.
    private static final int DIS_MODE_VAR_UPDATE_DISPLAY_DURATION = 1;

    // Which display mode the engine is currently using.
    private int displayMode = DIS_MODE_FIX_UPDATE_DISPLAY_DURATION;

    // Choreographer tells you a fake timeStamp for beginning of vsync. It really occurs at (frameTimeNanos - vsyncOffsetNanos).
    // This is not a huge deal, but if we can correct for it, why not?
    // Default it to 0 if our API level is not high enough to get the real value.
    private long vsyncOffsetNanos = 0;

    private double displayRefreshRate;

    public AbstractEngine(@UPS_Options int targetUPS, Screen screen) //target ups should divide evenly into 1000000000, updates are accurately called to within about 10ms
    {
        this.targetUPS = targetUPS;
        this.expectedUpdateTimeMS = 1000 / this.targetUPS;
        this.expectedUpdateTimeNS = this.expectedUpdateTimeMS * 1000000;

        gameStates = new LinkedList<>();

        gameScreen = screen;

        // Setup the 'Updates' thread.
        updateThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                Looper.prepare();
                updateThreadLooper = Looper.myLooper();
                runUpdates();

            }
        }, gameScreen.asView().getContext().getString(R.string.update_thread_name));

        // Setup the 'Frames' thread.
        frameThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                Looper.prepare();
                frameThreadLooper = Looper.myLooper();
                runFrames();
            }
        }, gameScreen.asView().getContext().getString(R.string.frame_thread_name));


        gameScreen.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // Humor the Android system.
                //v.performClick(); //TODO: maybe we should just remove this to save time? Is it really necessary anyway? Shouldn't the game swallow all input for itself?

                // Only use touch event if not paused
                if (isPlaying())
                {
                    onTouchEvent(event);
                    return true;
                }
                return false;
            }
        });

        //TODO: a different way to get it?
        /*

        DisplayInfo di = DisplayManagerGlobal.getInstance().getDisplayInfo(
                Display.DEFAULT_DISPLAY);
        return di.getMode().getRefreshRate();

         */
        //TODO: fallback if this cannot be done? (when the display returns null).
        Display display = ((WindowManager) gameScreen.asView().getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        displayRefreshRate = display.getRefreshRate();
        //L.d("Display Refresh Rate: " + displayRefreshRate, "optimization");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            vsyncOffsetNanos = display.getAppVsyncOffsetNanos();
        }
    }

    public void startGame()
    {
        if (getGameWidth() <= 0 || getGameHeight() <= 0)
        {
            throw new IllegalStateException("Cannot start the game before the screen has been given dimensions!");
        }
        launch();
    }

    private void launch()
    {
        if (started)
        {
            // May not start a game that is already started.
            throw new IllegalStateException("Game already started!");
        }
        else
        {
            started = true;

            // At this point, the surfaceView (and thus the game) has dimensions, so we can do initialization based on them.
            initialize();
            gameScreen.initialize();

            // We will launch two threads.
            // 1) Do game logic (game updates)
            // 2) Alert surface view (paint frames)
            updateThread.start();
            frameThread.start();
        }
    }

    /**
     * Called once before updates and frames begin.
     * In this call, we are guaranteed that the surfaceView (and thus the game) has dimensions,
     * so do any initialization that involves getGameWidth/getGameHeight.
     */
    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    protected abstract void initialize();

    private void runUpdates()
    {
        // Make sure that we are on updateThread.
        if (!Thread.currentThread().getName().equals(gameScreen.asView().getContext().getString(R.string.update_thread_name)))
        {
            throw new IllegalThreadStateException("Can only be called from updateThread!");
        }

        final Handler updateHandler = new Handler();

        Runnable updateCallback = new Runnable()
        {
            private boolean updateThreadPaused;
            private long startUpdateTimeNS = 0;

            private long debugLastUpdateTimeNS = 0L;
            private double debugUpdatesPerSecond;

            @Override
            public void run()
            {
                // Keep track of what time it is now. Goes first to get most accurate timing.
                startUpdateTimeNS = System.nanoTime();

                // Schedule next update. Goes second to get as accurate as possible updates.
                // We do it at the start to make sure we are waiting a precise amount of time
                // (as precise as we can get with postDelayed). This means we manually remove
                // the callback if the game stops.
                //updateHandler.removeCallbacksAndMessages(null);
                updateHandler.postDelayed(this, expectedUpdateTimeMS);

                // Track updates per second for debugging performance.
                debugUpdatesPerSecond = 1E9 / (startUpdateTimeNS - debugLastUpdateTimeNS);
                //L.d(Math.round(debugUpdatesPerSecond), "updatesPerSec");
                debugLastUpdateTimeNS = startUpdateTimeNS;

                // Acquire the monitor lock, because we cannot update the game at the same time we are trying to draw it.
                synchronized (monitorUpdateFrame)
                {
                    // Do a game update.
                    update();
                    screenTouched = false;

                    // Make new GameState with time stamp of the start of this update round.
                    GameState gameState = GameState.POOL.get();
                    gameState.setTimeStamp(startUpdateTimeNS);

                    // Save items to this game state for painting.
                    saveGameState(gameState);

                    // Execute interpolation service for all Entities and bind to this game state.
                    // Add the newly created Entities to this GameState.
                    saveInterpolationFields(gameState);

                    gameStates.add(gameState);

                    // Pause game (postDelayed runnable should not run while this thread is waiting, so no issues there)
                    while (pauseThreads)
                    {
                        try
                        {
                            if (!updateThreadPaused)
                            {
                                pauseLatch.countDown();
                            }
                            updateThreadPaused = true;
                            monitorUpdateFrame.wait();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    updateThreadPaused = false;

                    // Stop thread.
                    if (stopThreads)
                    {
                        updateHandler.removeCallbacksAndMessages(null);
                        stopLatch.countDown();
                        return;
                    }
                    //TODO: if updates are consistently taking too long (or even too short! [impossible?]) we can switch visualization modes.
                }
            }
        };

        updateHandler.post(updateCallback);
        Looper.loop();
    }

    private void saveInterpolationFields(GameState gameState)
    {
        /*L.d("=========================", "mem");
        L.d("Size of interp carrier pool before: " + InterpolatablesCarrier.POOL.debug(), "mem");
        L.d("Number of interpolable entities: " + Services.getServiceMembers(Services.Service.INTERPOLATION).size(), "mem");
*/
        // Run Interpolation Service. //TODO: call them systems? //TODO: on entities?
        for (Interpolatable interpolatable : Interpolatable.SERVICE.getMembers())
        {
            // Generate new InterpolatablesCarrier.
            InterpolatablesCarrier newInterpolatablesCarrier = InterpolatablesCarrier.POOL.get();
            interpolatable.provideInterpolatables(newInterpolatablesCarrier);

            // Save to gameState for this Entity.
            gameState.interps.put(interpolatable, newInterpolatablesCarrier);
        }
/*
        L.d("Size of interp carrier pool after_: " + InterpolatablesCarrier.POOL.debug(), "mem");*/
    }

    private void runFrames()
    {
        // Make sure that we are on frameThread.
        if (!Thread.currentThread().getName().equals(gameScreen.asView().getContext().getString(R.string.frame_thread_name)))
        {
            throw new IllegalThreadStateException("Can only be called from frameThread!");
        }

        vsync = Choreographer.getInstance();

        Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback()
        {
            private boolean frameThreadPaused;
            private long debugLastFrameTimeNS = 0L;
            private double debugFramesPerSecond;

            private long beginFrameThreadTimeMS;
            private boolean skipNextFrame = false;

            @Override
            public void doFrame(long frameTimeNanos)
            {
                beginFrameThreadTimeMS = SystemClock.currentThreadTimeMillis();

                // Correct for minor difference in vsync time.
                // This is probably totally unnecessary. (And will only change frameTimeNanos in a sufficiently high API anyway)
                frameTimeNanos -= vsyncOffsetNanos;

                /*long debugCurrentTimeNS = frameTimeNanos;
                debugFramesPerSecond = 1E9 / (debugCurrentTimeNS - debugLastFrameTimeNS);
                L.d(Math.round(debugFramesPerSecond), "framesPerSec");
                debugLastFrameTimeNS = debugCurrentTimeNS;*/

                // Frame skipping TODO: buggy due to skipping makes next frame appear faster, and one after is slower, and this creates a chain of skipping
                /*if (Math.round(debugFramesPerSecond) != 60)
                {
                    L.d("skip frame", "optimization");
                    vsync.postFrameCallback(this);
                    return;
                }*/

                synchronized (monitorUpdateFrame)
                {
                    // Skip a frame if last frame indicated that it took too long.
                    if (skipNextFrame)
                    {
                        skipNextFrame = false;
                        // If we plan on stopping or pausing during this frame, then
                        // we will not skip it, and instead continue as normal.
                        if (!stopThreads && !pauseThreads)
                        {
                            // Put ourselves up for the next frame...
                            vsync.postFrameCallback(this);
                            // ...and don't do any further work this frame.
                            return;
                        }
                    } //TODO: we may need to not skip if we just came out of a pause because the engine might misidentify that as a frame that took too long.

                    // Pause game.
                    // Spin lock when we want to pause.
                    while (pauseThreads)
                    {
                        try
                        {
                            // Do not count down the latch off spurious wakeup!
                            if (!frameThreadPaused)
                            {
                                if (gameScreen.hasPreparedPaint())
                                {
                                    // Unlock the canvas without posting anything.
                                    gameScreen.clearScreen(); //TODO: we should really be posting the most recent state, we don't want to clear the screen.
                                }
                                pauseLatch.countDown();
                            }
                            frameThreadPaused = true;
                            monitorUpdateFrame.wait();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    frameThreadPaused = false;

                    // Stop game if prompted.
                    if (stopThreads)
                    {
                        stopLatch.countDown();
                        return;
                    }

                    // Must ask for new callback each frame!
                    // We ask at the start because the Choreographer automatically
                    // skips frames for us if we don't draw fast enough,
                    // and it will make a Log.i to let us know that it skipped frames (so we know)
                    // If we move it to the end we essentially manually skip frames,
                    // but we won't know that an issue occurred.
                    vsync.postFrameCallback(this);

                    // Paint frame.
                    if (gameScreen.hasPreparedPaint())
                    {
                        GameState oldState;
                        GameState newState;

                        while (true)
                        {
                            // We need two states to draw, to interpolate between them.
                            if (gameStates.size() >= 2)
                            {
                                // Get the first two saved states.
                                oldState = gameStates.get(0);
                                newState = gameStates.get(1);

                                // Interpolate based on time that has past since the second active
                                // game state as a fraction of the time between the two active states.
                                double interpolationRatio;

                                // TODO: auto switch paint modes in response to update lag. priority=low
                                if (displayMode == DIS_MODE_FIX_UPDATE_DISPLAY_DURATION)
                                {
                                    interpolationRatio = (frameTimeNanos - newState.getTimeStamp()) / ((double) expectedUpdateTimeNS);
                                    /*if (interpolationRatio < 0)
                                    {
                                        Log.i("AbstractEngine", "interpolation ratio < 0: " + interpolationRatio);
                                    }*/
                                }
                                else if (displayMode == DIS_MODE_VAR_UPDATE_DISPLAY_DURATION)
                                {
                                    // Time that passed between the game states in question.
                                    long timeBetween = newState.getTimeStamp() - oldState.getTimeStamp();
                                    interpolationRatio = (frameTimeNanos - newState.getTimeStamp()) / ((double) timeBetween);
                                    /*if (interpolationRatio < 0)
                                    {
                                        Log.i("AbstractEngine", "interpolation ratio < 0: " + interpolationRatio);
                                    }*/
                                }
                                else
                                {
                                    throw new IllegalStateException("Engine is in an invalid displayMode.");
                                }

                                // If we are up to the new update, remove the old one as it is not needed.
                                if (interpolationRatio >= 1)
                                {
                                    // Remove the old update.
                                    if (gameStates.size() >= 1)
                                    {
                                        gameStates.get(0).cleanup(); //TODO: find way to clean up without causing error when lastVisualizedGameState is used
                                        GameState.POOL.put(gameStates.get(0));
                                        gameStates.remove(0);
                                    }
                                    continue;
                                }
                                else
                                {
                                    InterpolatablesCarrier interp;
                                    for (Interpolatable interpolatable : Interpolatable.SERVICE.getMembers())
                                    { //todo: we go through every item, even ones the game engine has given up on. but they have not been gc'd yet. this takes extra time... possible to fix this without necessitating that the owner marks the object as unused?
                                        InterpolatablesCarrier oldInterpolatablesCarrier = oldState.interps.get(interpolatable);
                                        InterpolatablesCarrier newInterpolatablesCarrier = newState.interps.get(interpolatable);

                                        if (oldInterpolatablesCarrier != null && newInterpolatablesCarrier != null)
                                        {
                                            try
                                            {
                                                interp = oldInterpolatablesCarrier.interpolateTo(newInterpolatablesCarrier, interpolationRatio);
                                                interpolatable.recallInterpolatables(interp);
                                                if (!interp.isEmpty())
                                                { //TODO: can this error happen? aren't non equal length in and out incompatible error'd?
                                                    throw new IllegalStateException(interpolatable + " not all interpolatables were recalled!");
                                                }
                                                interp.cleanup();
                                                InterpolatablesCarrier.POOL.put(interp);
                                            }
                                            catch (IllegalStateException e)
                                            {
                                                throw new IllegalStateException("Noncompatible interpolations.");
                                            }
                                        }
                                    }
                                    gameScreen.paintFrame(newState);
                                    break;
                                }
                            }
                            else
                            {
                                Log.i("Frames", "We want to draw but there aren't enough new updates!");
                                break;
                            }
                        }
                    }
                    else
                    { //TODO: remove this. no longer useful
                        Log.i("Frames", "Skipped painting frame because unprepared");
                    }
                }

                // Prepare for next frame now, when we have all the time in the world.
                // TODO: only do this if we actually have extra time, do not miss next frame callback! ? maybe.....
                if (gameScreen.hasInitialized() && !gameScreen.hasPreparedPaint())
                {
                    gameScreen.preparePaint();
                }

                // If we took more time than we are allowed, it may be that we are stuck
                // in a "death spiral", where we are constantly unable to catch up due to
                // the constant demand to generate the next frame. So we ease up a little bit
                // by skipping the next frame, so that we do not get screen jank.
                if (SystemClock.currentThreadTimeMillis() - beginFrameThreadTimeMS > 17) //TODO: not all phones are 60fps csync, replace with dynamically fetched value
                {
                    skipNextFrame = true;
                    Log.i(AbstractEngine.this.getClass().getSimpleName(),
                            "We took too long to generate this past frame, so we will skip " +
                                    "the next one to ease up on the load and avoid jank. If this " +
                                    "is happening a lot, your drawing routine may be doing too " +
                                    "much work!");
                }
            }
        };
        //Looper.myLooper().setMessageLogging(new LogPrinter(Log.DEBUG, "Looper"));
        vsync.postFrameCallback(frameCallback);
        Looper.loop();
    }


    /**
     * Stop update and frame threads.
     * Only return after the threads finish their current loop execution (stop completely).
     * After calling stopGame, the AbstractEngine is no longer usable.
     * Cleanup happens in this method.
     */
    public void stopGame()
    {
        synchronized (monitorControl)
        { //TODO: any reason not to move these checks outside the sync block?
            if (Thread.currentThread().getName().equals(gameScreen.asView().getContext().getString(R.string.update_thread_name)))
            {
                throw new IllegalThreadStateException("Cannot be called from "
                        + gameScreen.asView().getContext().getString(R.string.update_thread_name)
                        + " thread, because it cannot stop itself!");
            }
            else if (Thread.currentThread().getName().equals(gameScreen.asView().getContext().getString(R.string.frame_thread_name)))
            {
                throw new IllegalThreadStateException("Cannot be called from "
                        + gameScreen.asView().getContext().getString(R.string.frame_thread_name)
                        + " thread, because it cannot stop itself!");
            }

            if (stopThreads)
            {
                throw new IllegalStateException("Engine already in process of stopping!");
            }

            if (!isActive())
            {
                throw new IllegalStateException("Cannot stop if game is not active!");
            }

            // Check if we are paused.
            // If so, we must first pause the game before we can attempt to stop it.
            if (isPlaying())
            {
                // Will not return until the game is paused.
                pauseGame();
            }

            stopLatch = new CountDownLatch(NUMBER_OF_THREADS);
            stopThreads = true;

            // Now we know the threads are paused, and we have set them up to stop when they can.
            // We can finally resume the game so they can stop.
            resumeGame();

            try
            {
                stopLatch.await();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            // Make sure the threads have actually returned from their callbacks
            // before stopping the Loopers. This ensures that the Handlers can gracefully
            // finish processing their messages before we wrench the Loopers from their cold,
            // dead hands. Therefore, wait until we can grab the lock.
            synchronized (monitorUpdateFrame)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                {
                    updateThreadLooper.quitSafely();
                    frameThreadLooper.quitSafely();
                }
                else
                {
                    updateThreadLooper.quit();
                    frameThreadLooper.quit();
                }
            }

            stopped = true;
            stopThreads = false;
        }

        // Now cleanup all references.
        // After calling stopGame, this engine is no longer usable.
        gameScreen.cleanup();
        gameScreen = null;
        updateThread = null;
        updateThreadLooper = null;
        frameThread = null;
        frameThreadLooper = null;
        gameStates.clear(); //TODO: throw interps to the pool?

        //onStopGame();
    }

    /**
     * Pause the threads.
     */
    public void pauseGame()
    {
        synchronized (monitorControl)
        { //TODO: any reason not to move these checks outside the sync block?
            if (Thread.currentThread().getName().equals(gameScreen.asView().getContext().getString(R.string.update_thread_name)))
            {
                throw new IllegalThreadStateException("Cannot be called from "
                        + gameScreen.asView().getContext().getString(R.string.update_thread_name)
                        + " thread, because it cannot stop itself!");
            }
            else if (Thread.currentThread().getName().equals(gameScreen.asView().getContext().getString(R.string.frame_thread_name)))
            {
                throw new IllegalThreadStateException("Cannot be called from "
                        + gameScreen.asView().getContext().getString(R.string.frame_thread_name)
                        + " thread, because it cannot stop itself!");
            }

            if (pauseThreads)
            {
                throw new IllegalStateException("Engine already in process of pausing!");
            }

            if (!isActive())
            {
                throw new IllegalStateException("Cannot pause game that isn't running!");
            }

            pauseLatch = new CountDownLatch(NUMBER_OF_THREADS);

            // Tell threads to pause.
            pauseThreads = true;

            // Wait for threads to pause.
            // We do not need to worry that the threads have counted down the latch to 0 before
            // we actually call await on it, because await specifies that in such a case, it will
            // simply return immediately.
            try
            {
                pauseLatch.await();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            paused = true;
        }
    }

    public void resumeGame()
    {
        if (Thread.currentThread().getName().equals(gameScreen.asView().getContext().getString(R.string.update_thread_name)))
        {
            throw new IllegalThreadStateException("Cannot be called from "
                    + gameScreen.asView().getContext().getString(R.string.update_thread_name)
                    + " thread, because it cannot resume itself!");
        }
        else if (Thread.currentThread().getName().equals(gameScreen.asView().getContext().getString(R.string.frame_thread_name)))
        {
            throw new IllegalThreadStateException("Cannot be called from "
                    + gameScreen.asView().getContext().getString(R.string.frame_thread_name)
                    + " thread, because it cannot resume itself!");
        }
        if (!isActive())
        {
            throw new IllegalStateException("Cannot resume game that isn't active.");
        }
        if (isPlaying())
        {
            throw new IllegalStateException("Cannot resume game that isn't paused.");
        }

        //TODO: When we resume, time has passed, so push game states ahead because they are not invalid yet. (Visual fix).

        pauseThreads = false;
        paused = false;
        synchronized (monitorUpdateFrame)
        {
            monitorUpdateFrame.notifyAll();
        }
    }

    public boolean isActive()
    {
        return started && !stopped;
    }

    public boolean isPlaying()
    {
        return isActive() && !paused;
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    protected abstract void update();

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    protected abstract void saveGameState(GameState gameState); //TODO: remember, putting each update causes lots of memory use. (medium/low memory turnover spot)

    public int getGameWidth()
    {
        return gameScreen.getWidth();
    }
    public int getGameHeight()
    {
        return gameScreen.getHeight();
    }

    @CallSuper
    protected void onTouchEvent(MotionEvent event)
    {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN)
        {
            screenTouched = true;
        }
        else if (action == MotionEvent.ACTION_CANCEL)
        {
            screenTouched = false;
        }
    }

    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    protected Object getUpdateMonitor() //TODO: remove? standardize the synchronization first, and how input is handled, then make a decision.
    {
        return monitorUpdateFrame;
    }

    public abstract int getResult(); //TODO: remove eventually when we separate game engines from score game engines
}