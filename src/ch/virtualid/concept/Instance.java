package ch.virtualid.concept;

import ch.virtualid.database.Database;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Instances of this class can be {@link Observer observed} for certain {@link Aspect aspects}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Instance {
    
    /**
     * Adds the given observer to the sets of observers of the given aspects in the given map.
     * 
     * @param map the mapping from aspects to sets of observers.
     * @param observer the observer to be added.
     * @param aspects the aspects to be observed.
     * 
     * @require aspects.length > 0 : "At least one aspect is provided.";
     */
    private static void observe(@Nonnull Map<Aspect, Set<Observer>> map, @Nonnull Observer observer, @Nonnull Aspect... aspects) {
        assert aspects.length > 0 : "At least one aspect is provided.";
        
        for (final @Nonnull Aspect aspect : aspects) {
            @Nullable Set<Observer> observers = map.get(aspect);
            if (observers == null) {
                observers = new LinkedHashSet<Observer>();
                map.put(aspect, observers);
            }
            observers.add(observer);
        }
    }
    
    /**
     * Removes the given observer from the sets of observers of the given aspects in the given map.
     * 
     * @param map the mapping from aspects to sets of observers.
     * @param observer the observer to be removed.
     * @param aspects the aspects to be unobserved.
     * 
     * @require aspects.length > 0 : "At least one aspect is provided.";
     */
    private static void unobserve(@Nonnull Map<Aspect, Set<Observer>> map, @Nonnull Observer observer, @Nonnull Aspect... aspects) {
        assert aspects.length > 0 : "At least one aspect is provided.";
        
        for (final @Nonnull Aspect aspect : aspects) {
            final @Nullable Set<Observer> observers = map.get(aspect);
            if (observers != null) observers.remove(observer);
        }
    }
    
    /**
     * Notifies all the observers that observe the given aspect in the given map about the change in the given instance.
     * 
     * @param map the mapping from aspects to sets of observers.
     * @param aspect the aspect to notify the registered observers about.
     * @param instance the instance in which the change has occurred.
     * 
     * @require aspect.getClazz().isInstance(instance) : "The instance is an instance of the aspect's class.";
     */
    private static void notify(@Nonnull Map<Aspect, Set<Observer>> map, @Nonnull Aspect aspect, @Nonnull Instance instance) {
        assert aspect.getClazz().isInstance(instance) : "The instance is an instance of the aspect's class.";
        
        final @Nullable Set<Observer> observers = map.get(aspect);
        if (observers != null) {
            for (final @Nonnull Observer observer : observers) observer.notify(aspect, instance);
        }
    }
    
    
    /**
     * Stores the observers that observe an aspect independently of a particular instance.
     */
    private static final @Nonnull Map<Aspect, Set<Observer>> aspectObservers = new HashMap<Aspect, Set<Observer>>();
    
    /**
     * Observes the given aspects independently of a particular instance so that the given observer is notified on change.
     * 
     * @param observer the observer to be notified.
     * @param aspects the aspects to be observed.
     * 
     * @require aspects.length > 0 : "At least one aspect is provided.";
     * @require Database.isSingleAccess() : "The database is in single-access mode.";
     */
    public static void observeAspects(@Nonnull Observer observer, @Nonnull Aspect... aspects) {
        assert Database.isSingleAccess() : "The database is in single-access mode.";
        
        observe(aspectObservers, observer, aspects);
    }
    
    /**
     * Unobserves the given aspects independently of a particular instance so that the given observer is no longer notified on change.
     * 
     * @param observer the observer no longer to be notified.
     * @param aspects the aspects to be unobserved.
     * 
     * @require aspects.length > 0 : "At least one aspect is provided.";
     * @require Database.isSingleAccess() : "The database is in single-access mode.";
     */
    public static void unobserveAspects(@Nonnull Observer observer, @Nonnull Aspect... aspects) {
        assert Database.isSingleAccess() : "The database is in single-access mode.";
        
        unobserve(aspectObservers, observer, aspects);
    }
    
    
    /**
     * Stores the observers that observe an aspect of this instance.
     */
    private @Nullable Map<Aspect, Set<Observer>> instanceObservers = null;
    
    /**
     * Observes the given aspects of this instance so that the given observer is notified on change.
     * 
     * @param observer the observer to be notified.
     * @param aspects the aspects to be observed.
     * 
     * @require aspects.length > 0 : "At least one aspect is provided.";
     * @require Database.isSingleAccess() : "The database is in single-access mode.";
     * @require for (Aspect aspect : aspects) aspect.getClazz().isInstance(this) : "This is an instance of the aspect's class.";
     */
    public final void observe(@Nonnull Observer observer, @Nonnull Aspect... aspects) {
        assert Database.isSingleAccess() : "The database is in single-access mode.";
        for (final @Nonnull Aspect aspect : aspects) assert aspect.getClazz().isInstance(this) : "This is an instance of the aspect's class.";
        
        if (instanceObservers == null) instanceObservers = new HashMap<Aspect, Set<Observer>>();
        observe(instanceObservers, observer, aspects);
    }
    
    /**
     * Unobserves the given aspects of this instance so that the given observer is no longer notified on change.
     * 
     * @param observer the observer no longer to be notified.
     * @param aspects the aspects to be unobserved.
     * 
     * @require aspects.length > 0 : "At least one aspect is provided.";
     * @require Database.isSingleAccess() : "The database is in single-access mode.";
     * @require for (Aspect aspect : aspects) aspect.getClazz().isInstance(this) : "This is an instance of the aspect's class.";
     */
    public final void unobserve(@Nonnull Observer observer, @Nonnull Aspect... aspects) {
        assert Database.isSingleAccess() : "The database is in single-access mode.";
        for (final @Nonnull Aspect aspect : aspects) assert aspect.getClazz().isInstance(this) : "This is an instance of the aspect's class.";
        
        if (instanceObservers != null) unobserve(instanceObservers, observer, aspects);
    }
    
    /**
     * Notifies all the observers that observe the given aspect – both of this instance and independently of any instance.
     * Please note that the notification mechanism is only enabled if the database is in single-access mode.
     * 
     * @param aspect the aspect to notify the registered observers about.
     * 
     * @require aspect.getClazz().isInstance(this) : "This is an instance of the aspect's class.";
     */
    protected final void notify(@Nonnull Aspect aspect) {
        assert aspect.getClazz().isInstance(this) : "This is an instance of the aspect's class.";
        
        if (Database.isSingleAccess()) {
            if (instanceObservers != null) notify(instanceObservers, aspect, this);
            notify(aspectObservers, aspect, this);
        }
    }
    
}
