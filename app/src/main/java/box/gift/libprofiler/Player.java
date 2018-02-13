package box.gift.libprofiler;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

import box.shoe.gameutils.Entity;
import box.shoe.gameutils.EntityServices;
import box.shoe.gameutils.GameEvents;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.Vector;
import box.shoe.gameutils.Weaver;

/**
 * Created by Joseph on 2/9/2018.
 */

public class Player extends Entity implements Paintable
{
    private static final Vector VELOCITY_SHORT_JUMP = new Vector(0, -77);
    private static final Vector VELOCITY_JUMP = new Vector(0, -90);
    private static final Vector ACCELERATION_GRAVITY = new Vector(0, 10.5);

    private Paintable paintable;

    private boolean doubleJumped = true;
    public boolean grounded = false;

    private static final int attackCooldown = 12;
    private int sinceAttack = attackCooldown;

    public Player(float initialX, float initialY, float initialHeight, float initialWidth)
    {
        super(initialX, initialY, initialWidth, initialHeight);
        acceleration = ACCELERATION_GRAVITY;
        paintable = new PlayerPaintable();
        EntityServices.addService(this, EntityServices.Service.INTERPOLATION);
    }
/*
    public Attack requestAttack()
    {
        if (sinceAttack >= attackCooldown)
        {
            sinceAttack = 0;
            return attack();
        }
        else
        {
            return null;
        }
    }

    private Attack attack()
    {
        return new Attack(x + width, y, width, height);
    }
*/
    public void requestJump()
    {
        if (grounded)
        {
            jump();
        }
        else if (!doubleJumped)
        {
            shortJump();
            doubleJumped = true;
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
        sinceAttack += 1;
        sinceAttack = Math.min(sinceAttack, attackCooldown);
    }

    @Override
    public void paint(Canvas canvas, Resources resources)
    {
        paintable.paint(canvas, resources);
    }

    private class PlayerPaintable implements Paintable
    {
        private boolean started = false;
        private int ind = 0;
        private AnimationDrawable runAnimation;
        private Drawable still;

        @Override
        public void paint(Canvas canvas, Resources resources)
        {
            if (!started)
            {
                runAnimation = (AnimationDrawable) resources.getDrawable(R.drawable.run_animation);
                started = true;
            }

            if (grounded)
            {
                runAnimation.setBounds((int) _x, (int) _y, (int) (_x + _width), (int) (_y + _height));
                runAnimation.selectDrawable(ind / 4);
                ind++;
                if (ind > 28) ind = 0;
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
                still.setBounds((int) _x, (int) _y, (int) (_x + _width), (int) (_y + _height));
                still.draw(canvas);
            }
            //canvas.drawRect(_x, _y, _x + _width, _y + _height, paint);
        }
    }
}
