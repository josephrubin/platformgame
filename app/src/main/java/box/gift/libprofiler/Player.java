package box.gift.libprofiler;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

import box.shoe.gameutils.DisplayEntity;
import box.shoe.gameutils.effects.BurstParticleEffect;
import box.shoe.gameutils.engine.Interpolatable;
import box.shoe.gameutils.Vector;

/**
 * Created by Joseph on 2/9/2018.
 */

public class Player extends DisplayEntity
{
    private static final Vector VELOCITY_SHORT_JUMP = new Vector(0, -77);
    private static final Vector VELOCITY_JUMP = new Vector(0, -90);
    private static final Vector ACCELERATION_GRAVITY = new Vector(0, 10.5);

    private PlayerPaintable paintable;

    private boolean doubleJumped = true;
    public boolean grounded = false;

    // Particles.
    private BurstParticleEffect jumpEffect;

    public Player(float initialX, float initialY, float initialHeight, float initialWidth)
    {
        super(initialX, initialY, initialWidth, initialHeight);
        acceleration = ACCELERATION_GRAVITY;
        paintable = new PlayerPaintable();
        Interpolatable.SERVICE.addMember(this);

        // Effects.
        jumpEffect = new BurstParticleEffect.Builder()
                .color(Color.BLACK)
                .size(5)
                .speed(30)
                .duration(4)
                .particleCount(18)
                .build();
    }

    public void requestJump()
    {
        if (grounded)
        {
            jump();
            //jumpEffect.produce(body.centerX(), body.bottom);
        }
        else if (!doubleJumped)
        {
            shortJump();
            doubleJumped = true;
            //jumpEffect.produce(body.centerX(), body.bottom);
        }
    }

    private void jump()
    {
        velocity = VELOCITY_JUMP;
    }

    private void shortJump()
    {
        velocity = VELOCITY_SHORT_JUMP;
    }

    public void onGround()
    {
        grounded = true;
        doubleJumped = false;
    }
    public void offGround()
    {
        grounded = false;
    }

    @Override
    public void update()
    {
        super.update();
        //jumpEffect.update();
    }

    public void paint(Canvas canvas, Resources resources)
    {
        paintable.paint(canvas, resources);
        //jumpEffect.paint(canvas, resources);
    }

    private class PlayerPaintable
    {
        private Rect bounds = new Rect();
        private boolean started = false;
        private int currentDrawableIndex = 0;
        private AnimationDrawable runAnimation;
        private Drawable still;

        public void paint(Canvas canvas, Resources resources)
        {
            if (!started)
            {
                runAnimation = (AnimationDrawable) resources.getDrawable(R.drawable.run_animation);
                started = true;
            }

            display.round(bounds);
            if (grounded)
            {
                runAnimation.setBounds(bounds);
                runAnimation.selectDrawable(currentDrawableIndex / 4);
                currentDrawableIndex++;
                if (currentDrawableIndex > 28) currentDrawableIndex = 0;
                runAnimation.draw(canvas);
            }
            else
            {
                if (!doubleJumped)
                {
                    still = resources.getDrawable(R.drawable.run3);
                }
                else
                {
                    still = resources.getDrawable(R.drawable.run7);
                }
                still.setBounds(bounds);
                still.draw(canvas);
            }
            //canvas.drawRect(_x, _y, _x + _width, _y + _height, paint);
        }
    }
}
