package box.shoe.gameutils.emitter;

import box.shoe.gameutils.DisplayEntity;

/**
 * Created by Joseph on 2/21/2018.
 */
//TODO: good name or not? simpleEmitter? etc.
public class DisplayEntityEmitter implements Emitter<DisplayEntity>
{
    private DisplayEntity.Factory factory;
    private float x;
    private float y;

    public DisplayEntityEmitter(DisplayEntity.Factory factory)
    {
        this.factory = factory;
    }

    // javadoc: set center x point etc etc
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

    /**
     * Returns a DisplayEntity whose body's center point is at the specified position
     * given by {@link #setX(float)} and {@link #setY(float)}.
     * @return the emitted DisplayEntity.
     */
    @Override
    public DisplayEntity emit()
    {
        DisplayEntity displayEntity = factory.create();
        displayEntity.body.offsetCenterTo(x, y);
        displayEntity.display.set(displayEntity.body); //TODO: standardize how this happens... should we even expect all clients to do this? (no, I would think)
        return displayEntity;
    }
}
