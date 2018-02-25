package box.shoe.gameutils.engine;

import android.support.annotation.CallSuper;

import box.shoe.gameutils.Service;

/**
 * Created by Joseph on 2/14/2018.
 */

public interface Interpolatable
{
    // Service.
    Service<Interpolatable> SERVICE = new Service<>();

    @CallSuper
    void provideInterpolatables(InterpolatablesCarrier in);
    @CallSuper
    void recallInterpolatables(InterpolatablesCarrier out);
}
