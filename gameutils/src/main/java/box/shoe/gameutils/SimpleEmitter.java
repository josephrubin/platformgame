package box.shoe.gameutils;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Joseph on 12/19/2017.
 */

public class SimpleEmitter implements Emitter
{
    private Paint paint;
    private HashMap<DisplayEntity, Integer> particles;
    private float size;
    private double speed;
    private double startRadians;
    private double endRadians;
    private int duration;
    private float x;
    private float y;

    private SimpleEmitter(float size, double speed, int color, double startRadians, double endRadians, int duration)
    {
        this.size = size;
        this.speed = speed;
        this.startRadians = startRadians;
        this.endRadians = endRadians;
        this.duration = duration;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        particles = new HashMap<>();
    }

    public void update()
    {
        Iterator<Map.Entry<DisplayEntity, Integer>> entryIterator = particles.entrySet().iterator();
        while (entryIterator.hasNext())
        {
            Map.Entry<DisplayEntity, Integer> entry = entryIterator.next();
            if (entry.getValue() >= duration)
            {
                entry.getKey().cleanup();
                entryIterator.remove();
            }
            else
            {
                entry.getKey().update();
                entry.setValue(entry.getValue() + 1);
            }
        }
    }

    public void paint(Canvas canvas, Resources resources)
    {
        for (DisplayEntity particle : particles.keySet())
        {
            canvas.drawRect(particle.display, paint);
        }
    }

    @Override
    public void setX(float x)
    {
        this.x = x;
    }

    @Override
    public void setY(float y)
    {
        this.y = y;
    }

    @Override
    public void emit()
    {
        DisplayEntity particle = new DisplayEntity(x - size / 2, y - size / 2, size, size);
        Interpolatable.SERVICE.addMember(particle); //TODO: remove particles services on kill
        particle.velocity = Vector.fromPolarDegrees(speed, Rand.instance.intFrom((int) Math.toDegrees(startRadians), (int) Math.toDegrees(endRadians))); //TODO: we are turning deg into rad then back then forth. find better way (priority=low)
        particles.put(particle, 0);
    }

    public static class Builder
    {
        private int duration = 1000;
        private double speed = 5;
        private float size = 20;
        private int color;
        private double startRadians;
        private double endRadians;

        public Builder()
        {

        }

        public SimpleEmitter build()
        {
            return new SimpleEmitter(size, speed, color, startRadians, endRadians, duration);
        }

        public Builder speed(double particleSpeed)
        {
            this.speed = particleSpeed;
            return this;
        }

        public Builder duration(int duration)
        {
            this.duration = duration;
            return this;
        }

        public Builder color(int color)
        {
            this.color = color;
            return this;
        }

        //Width/Height or square
        public Builder size(float size)
        {
            this.size = size;
            return this;
        }

        public Builder spanDegrees(double startDegrees, double endDegrees)
        {
            return spanRadians(Math.toRadians(startDegrees), Math.toRadians(endDegrees));
        }

        public Builder spanRadians(double startRadians, double endRadians)
        {
            this.startRadians = startRadians;
            this.endRadians = endRadians;
            return this;
        }
    }
}
