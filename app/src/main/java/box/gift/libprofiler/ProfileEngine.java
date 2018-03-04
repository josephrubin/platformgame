package box.gift.libprofiler;

import android.view.Choreographer;
import android.view.MotionEvent;

import java.util.Iterator;
import java.util.LinkedList;

import box.shoe.gameutils.engine.AbstractEngine;
import box.shoe.gameutils.GameEvents;
import box.shoe.gameutils.engine.GameState;
import box.shoe.gameutils.Rand;
import box.shoe.gameutils.map.MapLoader;
import box.shoe.gameutils.screen.Screen;
import box.shoe.gameutils.Vector;
import box.shoe.gameutils.Weaver;

/**
 * Created by Joseph on 2/8/2018.
 */

public class ProfileEngine extends AbstractEngine
{
    public static final int TARGET_UPS = 25;

    public static final Vector NORMAL_SCROLL_SPEED = new Vector(-38, 0);
    public static final Vector FAST_SCROLL_SPEED = new Vector(-250, 0);

    public static final String PLAYER = "player";
    public static final String PLATFORMS = "platforms";
    public static final String SCORE = "score";
    public static final String TRY_AGAIN_BUTTON = "try_again_button";
    public static final String PLAY_BUTTON = "play_button";

    private Player player;
    private LinkedList<Platform> platforms;
    private Platform initialPlatform;
    private float lastPlatformY;

    private TryAgainButton tryAgainButton;
    private PlayButton playButton;
    private boolean firstTry = true;

    private final int platformDistance = 450;

    private Rand random;

    private int score;

    private int difficulty;
    private final int SECONDS_UNTIL_MAX_DIFFICULTY = 35;

    private boolean tryAgain = false;
    private boolean tryAgainButtonTouched = false;

    private boolean playerDied = true;

    public ProfileEngine(Screen screen)
    {
        super(TARGET_UPS, screen);
        platforms = new LinkedList<>();
        random = new Rand();
    }

    @Override
    protected void initialize()
    {
        createPlayer();
        spawnInitialPlatform();

        score = 0;
        difficulty = 0;
    }

    private void createPlayer()
    {
        float playerHeight = getGameWidth() / 10;
        player = new Player(getGameWidth() / 15, 0 - playerHeight, playerHeight, playerHeight * (21F/33F));
    }


    @Override
    protected void update()
    {
        if (!tryAgain)
        {
            difficulty += 1;
            difficulty = Math.min(difficulty, TARGET_UPS * SECONDS_UNTIL_MAX_DIFFICULTY);

            if (screenTouched)
            {
                player.requestJump();
            }

            //TODO: player dying should be part of its own update method, but we must first figure out how it knows when it's offscreen.
            if (player.body.top > getGameHeight())
            {
                if (!playerDied)
                {
                    spawnInitialPlatform();
                    for (Platform platform : platforms)
                    {
                        platform.velocity = FAST_SCROLL_SPEED;
                    }
                    playerDied = true;
                }
            }

            float playerOldBottom = player.body.bottom;
            if (!playerDied)
            {
                player.update();
            }
            player.offGround();

            boolean spawnPlatform = false;
            Iterator<Platform> platformIterator = platforms.iterator();
            while (platformIterator.hasNext())
            {
                Platform platform = platformIterator.next();

                if (playerOldBottom <= platform.body.top && player.body.bottom > platform.body.top
                        && player.body.right > platform.body.left && player.body.left < platform.body.right)
                {
                    player.body.offsetTo(player.body.left, platform.body.top - player.body.height());
                    player.velocity = Vector.ZERO;
                }
                if (player.body.bottom == platform.body.top
                        && player.body.right + 50 > platform.body.left && player.body.left < platform.body.right + 50 /*add a bit of tolerance if we have just left the platform*/
                        && player.velocity.getY() >= 0)
                {/*
                if (!grounded)
                {
                    for (int i = 0; i < 12; i++)
                    {
                        landEmitter.emit(player.x + player.width / 2, player.y + player.height);
                    }
                }*/
                    player.onGround();
                }

                // Destroy offscreen platforms.
                if (platform.body.right < 0)
                {
                    platform.cleanup();
                    platformIterator.remove();
                }

                platform.update();

                // Check if this is the last platform.
                if (!platformIterator.hasNext())
                {
                    if (platform.body.right + platformDistance <= getGameWidth())
                    {
                        spawnPlatform = true;
                    }
                }
            }
            if (spawnPlatform && !playerDied)
            {
                spawnPlatform();
            }

            if (!playerDied)
            {
                score += 1;
            }
        }

        if (initialPlatform != null && initialPlatform.body.left <= 0)
        {
            float size = getGameWidth() / 3;
            if (firstTry)
            {
                playButton = new PlayButton(getGameWidth() / 2 - size / 2, getGameHeight() / 2 - size / 2, size, size);
                firstTry = false;
            }
            else
            {
                tryAgainButton = new TryAgainButton(getGameWidth() / 2 - size / 2, getGameHeight() / 2 - size / 2, size, size);
            }
            initialPlatform = null;

            tryAgain = true;
        }

        if (tryAgainButtonTouched)
        {
            playerDied = false;
            createPlayer();
            score = 0;
            difficulty = 0;
            tryAgainButton = null;
            playButton = null;
            tryAgain = false;
            for (Platform platform : platforms)
            {
                platform.velocity = NORMAL_SCROLL_SPEED;
            }
        }

        tryAgainButtonTouched = false;
    }

    private void spawnInitialPlatform()
    {
        initialPlatform = new Platform(getGameWidth() * 1.4F, 4 * getGameHeight() / 9, getGameWidth() * 2, getGameHeight() / 34, FAST_SCROLL_SPEED);
        platforms.add(initialPlatform);
        lastPlatformY = getGameHeight() / 2;
    }

    private void spawnPlatform()
    {
        int marginTopY = getGameHeight() / 9;
        int marginBotY = getGameHeight() / 5;

        if (lastPlatformY - marginTopY > 300)
        {
            marginTopY = (int) (lastPlatformY - 300);
        }

        spawnPlatform(marginTopY, getGameHeight() - marginBotY, false);
    }

    private void spawnPlatform(int yMin, int yMax, boolean secondPlatform)
    {
        // Platform height.
        int h = getGameHeight() / 34;

        // Random y pos.
        double favorLowerFactor = 0.87;
        int y = (int) Math.max(yMin, random.intFrom(yMin, yMax - h) * favorLowerFactor);

        // Random x pos factor.
        int rand = random.intFrom(0, secondPlatform ? 6 : 4); // Second platforms will tend to be farther.
        float x = getGameWidth();
        x *= (1 + rand / 10F);

        // Random width.
        int initialWidth = secondPlatform ? 250 : 500; // Second platforms will tend to be smaller.
        int randomFactor = random.intFrom(220, 900);
        double difficultyFactor = (double) difficulty / (TARGET_UPS * SECONDS_UNTIL_MAX_DIFFICULTY);
        int w = (int) (((double) initialWidth * (1 - difficultyFactor)) + randomFactor);

        // Create the platform
        platforms.add(new Platform(x, y, w, h, NORMAL_SCROLL_SPEED));

        lastPlatformY = y;
        // Maybe generate a second platform.
        if (!secondPlatform)
        {
            if (y < getGameHeight() / 2)
            {
                if (random.intFrom(0, 5) == 0)
                {
                    // Spawn a platform in the bottom half.
                    spawnPlatform(y + getGameHeight() / 5, yMax, true);
                }
            }
        }
    }

    @Override
    protected void saveGameState(GameState gameState)
    {
        gameState.put(PLAYER, player);
        gameState.put(PLATFORMS, platforms);
        gameState.put(SCORE, score / 2);
        gameState.put(TRY_AGAIN_BUTTON, tryAgainButton);
        gameState.put(PLAY_BUTTON, playButton);
    }

    @Override
    protected void onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);

        if (screenTouched)
        {
            if (tryAgainButton != null)
            {
                if (tryAgainButton.body.contains(event.getX(), event.getY()))
                {
                    tryAgainButtonTouched = true;
                }
            }
            else if (playButton != null)
            {
                if (playButton.body.contains(event.getX(), event.getY()))
                {
                    tryAgainButtonTouched = true;
                }
            }
        }
    }

    //TODO: make this score set the best, even if we didn't make the game stop.
    @Override
    public int getResult()
    {
        return score / 2;
    }
}
