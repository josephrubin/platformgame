package box.shoe.gameutils;

import java.util.Random;

public class Rand extends Random //TODO: should not be instantiated. simply supply static utility methods for using Math.random.
{
    /*pack*/ static final Rand instance = new Rand();

    public Rand()
    {
        super();
    }

    public Rand(long seed)
    {
        super(seed);
    }

    public int intFrom(int min, int max)
    {
        return nextInt(max + 1 - min) + min;
    }
}
