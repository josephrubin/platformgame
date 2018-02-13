package box.shoe.gameutils;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by Joseph on 2/8/2018.
 */

public class EntityServices
{ //TODO: replace enum with annotation interface, or constants, because enums on android are REALLY BAD!
    public enum Service
    {
        INTERPOLATION(interpolation),
        PHYSICS(physics);

        private Set<Entity> relatedSet;

        Service(Set<Entity> relatedSet)
        {
            this.relatedSet = relatedSet;
        }
    }

    // The sets are weak, but an error will be thrown if an Entity is GC'd before its cleaned up.
    private final static Set<Entity> interpolation = Collections.newSetFromMap(new WeakHashMap<Entity, Boolean>());
    private final static Set<Entity> physics = Collections.newSetFromMap(new WeakHashMap<Entity, Boolean>());

    public static void addService(Entity entity, Service service)
    {
        service.relatedSet.add(entity);
    }

    public static void removeService(Entity entity, Service service)
    {
        if (!service.relatedSet.contains(entity))
        {
            throw new IllegalStateException("Supplied Entity does not have the service " + service.name() + " to remove!");
        }
        service.relatedSet.remove(entity);
    }

    public static void removeAllServices(Entity entity)
    {
        for (Service service : Service.values())
        {
            service.relatedSet.remove(entity);
        }
    }

    public static boolean hasService(Entity entity, Service service)
    {
        return service.relatedSet.contains(entity);
    }

    /*pack*/ static boolean hasServices(Entity entity)
    {
        for (Service service : Service.values())
        {
            if (service.relatedSet.contains(entity))
                return true;
        }
        return false;
    }

    public static Set<Entity> getServiceMembers(Service service)
    {
        return service.relatedSet;
    }
}
