package box.shoe.gameutils;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by Joseph on 2/14/2018.
 */

public class Service<I>
{
    private final Set<I> members = Collections.newSetFromMap(new WeakHashMap<I, Boolean>());

    public void addMember(I implementor)
    {
        members.add(implementor);
    }

    public void removeMember(I implementor)
    {
        if (!members.contains(implementor))
        {
            throw new IllegalStateException("Supplied object does not have the service " + getClass().getName() + " to remove!");
        }
        members.remove(implementor);
    }

    public boolean hasMember(I implementor)
    {
        return members.contains(implementor);
    }

    public Set<I> getMembers()
    {
        return members;
    }
}
