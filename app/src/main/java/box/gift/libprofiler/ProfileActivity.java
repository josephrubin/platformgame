package box.gift.libprofiler;

import android.content.Context;

import box.shoe.gameutils.AbstractEngine;
import box.shoe.gameutils.AbstractGameActivity;
import box.shoe.gameutils.Screen;

/**
 * Created by Joseph on 2/8/2018.
 */

public class ProfileActivity extends AbstractGameActivity
{
    @Override
    protected Screen provideNewScreen(Context context, Runnable readyForPaintingListener)
    {
        return new ProfileScreen(context, readyForPaintingListener);
    }

    @Override
    protected AbstractEngine provideNewEngine(Screen screen)
    {
        return new ProfileEngine(screen);
    }

    @Override
    protected int provideMainMenuLayoutResId()
    {
        return R.layout.main_menu;
    }

    @Override
    protected int providePauseMenuLayoutResId()
    {
        return 0;
    }

    @Override
    protected int provideScoreTextViewIdResId()
    {
        return R.id.score;
    }

    @Override
    protected int provideBestTextViewIdResId()
    {
        return R.id.best;
    }

    @Override
    protected int provideGameSplashColorId()
    {
        return 0;
    }
}
