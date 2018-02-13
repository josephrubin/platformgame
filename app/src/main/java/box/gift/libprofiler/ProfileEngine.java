package box.gift.libprofiler;

import android.graphics.Color;
import android.view.MotionEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import box.shoe.gameutils.AbstractEngine;
import box.shoe.gameutils.Emitter;
import box.shoe.gameutils.Entity;
import box.shoe.gameutils.EntityCollisions;
import box.shoe.gameutils.EntityServices;
import box.shoe.gameutils.GameEvents;
import box.shoe.gameutils.GameState;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.Rand;
import box.shoe.gameutils.Screen;
import box.shoe.gameutils.SimpleEmitter;
import box.shoe.gameutils.TaskScheduler;
import box.shoe.gameutils.Vector;
import box.shoe.gameutils.Weaver;

/**
 * Created by Joseph on 2/8/2018.
 */

public class ProfileEngine extends AbstractEngine
{
    public static final int TARGET_UPS = 25;

    public static final Vector SCROLL_SPEED = new Vector(-38, 0);

    public static final String PLAYER = "player";
    public static final String PLATFORMS = "platforms";
    public static final String ENEMIES = "enemies";
    public static final String LAND_EMITTER = "landEmitter";
    public static final String SCORE = "score";
    public static final String ATTACK = "attack";

    private Player player;
    private LinkedList<Enemy> enemies;
    private LinkedList<Platform> platforms;
    private float lastPlatformY;
    //private Attack attack = null;

    private final int platformDistance = 450;

    private Rand random;

    // Particles.
    private Emitter landEmitter;
    private Emitter jumpEmitter;

    private int score = 0;

    private int difficulty = 0;
    private final int SECONDS_UNTIL_MAX_DIFFICULTY = 35;

    //private boolean jumpTouch = false;

    public ProfileEngine(Screen screen)
    {
        super(TARGET_UPS, screen);
        enemies = new LinkedList<>();
        platforms = new LinkedList<>();
        random = new Rand();

        landEmitter = new SimpleEmitter.Builder()
                .color(Color.LTGRAY)
                .duration(4)
                .speed(12)
                .spanDegrees(225, 315)
                .size(15)
                .build();
    }

    @Override
    protected void initialize()
    {
        float playerHeight = getGameWidth() / 10;
        player = new Player(getGameWidth() / 15, 4 * getGameHeight() / 9 - playerHeight, playerHeight, playerHeight * (21F/33F));

        // Spawn initial platform.
        platforms.add(new Platform(0, 4 * getGameHeight() / 9, getGameWidth() * 2, getGameHeight() / 34));
        lastPlatformY = getGameHeight() / 2;
    }

    @Override
    protected void update()
    {
        difficulty += 1;
        difficulty = Math.min(difficulty, TARGET_UPS * SECONDS_UNTIL_MAX_DIFFICULTY);

        if (screenTouched)
        {
            player.requestJump();
        }

        //TODO: player dying should be part of its own update method, but we must first figure out how it knows when it's offscreen.
        if (player.y > getGameHeight())
        {
            Weaver.tug(GameEvents.GAME_OVER);
        }

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext())
        {
            Enemy enemy = enemyIterator.next();
            /*if (attack != null && EntityCollisions.entityEntity(attack, enemy))
            {
                enemyIterator.remove();
            }
            else */if (EntityCollisions.entityEntity(player, enemy))
            {
                Weaver.tug(GameEvents.GAME_OVER);
            }

            // Destroy offscreen enemies.
            else if (enemy.x + enemy.width < 0)
            {
                enemy.cleanup();
                enemyIterator.remove();
            }

            enemy.update();
        }

        float playerOldY = player.y;
        boolean grounded = player.grounded;
        player.update();
        player.offGround();

        boolean spawnPlatform = false;
        Iterator<Platform> platformIterator = platforms.iterator();
        while (platformIterator.hasNext())
        {
            Platform platform = platformIterator.next();

            if (playerOldY + player.height <= platform.y && player.y + player.height > platform.y
                    && player.x + player.width > platform.x && player.x < platform.x + platform.width)
            {
                player.y = platform.y - player.height;
                player.velocity = Vector.ZERO;
            }/*
            else if (EntityCollisions.entityEntity(player, platform))
            {
                Weaver.tug(GameEvents.GAME_OVER);
            }*/
            if (player.y + player.height == platform.y
                    && player.x + player.width + 50 > platform.x && player.x < platform.x + platform.width + 50 /*add a bit of tolerance if we have just left the platform*/
                    && player.velocity.getY() >= 0)
            {
                if (!grounded)
                {
                    for (int i = 0; i < 12; i++)
                    {
                        landEmitter.emit(player.x + player.width / 2, player.y + player.height);
                    }
                }
                player.onGround();
            }

            // Destroy offscreen platforms.
            if (platform.x + platform.width < 0)
            {
                platform.cleanup();
                platformIterator.remove();
            }

            platform.update();

            // Check if this is the last platform.
            if (!platformIterator.hasNext())
            {
                if (platform.x + platform.width + platformDistance <= getGameWidth())
                {
                    spawnPlatform = true;
                }
            }
        }
        if (spawnPlatform)
        {
            spawnPlatform();
        }
/*
        if (attack != null)
        {
            attack.cleanup();
            attack = null;
        }
        if (screenTouched)
        {
            if (jumpTouch)
            {
                player.requestJump();
            }
            else
            {
                attack = player.requestAttack();
            }
            jumpTouch = false;
        }
*/
        landEmitter.update();

        score += 1;
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
        double favorLowerFactor = 0.9;
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
        platforms.add(new Platform(x, y, w, h));

        lastPlatformY = y;
/*
        // Maybe create an enemy, if the platform is long enough.
        if (w > 500 && random.intFrom(0, 1) == 0)
        {
            int enemyHeight = getGameHeight() / 30;
            int enemyWidth = enemyHeight;
            enemies.add(new Enemy(x + (2 * w / 3) - (enemyWidth / 2), y - enemyHeight, enemyWidth, enemyHeight));
        }
*/
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
        gameState.put(ENEMIES, enemies);
        gameState.put(LAND_EMITTER, landEmitter);
        gameState.put(SCORE, score / 2);
        //gameState.put(ATTACK, attack);
    }
/*
    @Override
    protected void onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);

        if (screenTouched)
        {
            if (event.getX() <= getGameWidth() / 2)
            {
                jumpTouch = true;
            }
        }
    }
*/
    @Override
    public int getResult()
    {
        return score / 2;
    }
}
