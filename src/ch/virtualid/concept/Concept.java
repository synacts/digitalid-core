package ch.virtualid.concept;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.ClientEntity;
import ch.virtualid.entity.Entity;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an abstract {@link Concept concept} in the {@link Database database}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Concept {
    
    /**
     * Stores the entity to which this concept belongs or null if it is impersonal.
     */
    private final @Nullable Entity entity;
    
    /**
     * Creates a new concept with the given entity.
     * 
     * @param entity the entity to which this concept belongs or null if it is impersonal.
     */
    protected Concept(@Nullable Entity entity) {
        this.entity = entity;
    }
    
    /**
     * Returns the entity to which this concept belongs or null if it is impersonal.
     * 
     * @return the entity to which this concept belongs or null if it is impersonal.
     */
    @Pure
    public final @Nullable Entity getEntity() {
        return entity;
    }
    
    
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
     * Notifies all the observers that observe the given aspect in the given map about the change in the given concept.
     * 
     * @param map the mapping from aspects to sets of observers.
     * @param aspect the aspect to notify the registered observers about.
     * @param concept the concept in which the change has occurred.
     */
    private static void notify(@Nonnull Map<Aspect, Set<Observer>> map, @Nonnull Aspect aspect, @Nonnull Concept concept) {
        final @Nullable Set<Observer> observers = map.get(aspect);
        if (observers != null) {
            for (final @Nonnull Observer observer : observers) observer.notify(aspect, concept);
        }
    }
    
    
    /**
     * Stores the observers that observe an aspect independently of a particular instance.
     */
    private static final @Nonnull Map<Aspect, Set<Observer>> aspectObservers = new HashMap<Aspect, Set<Observer>>();
    
    /**
     * Observes the given aspects independently of a particular instance and notifies the given observer on change.
     * 
     * @param observer the observer to be notified.
     * @param aspects the aspects to be observed.
     * 
     * @require aspects.length > 0 : "At least one aspect is provided.";
     */
    public static void observeAspects(@Nonnull Observer observer, @Nonnull Aspect... aspects) {
        observe(aspectObservers, observer, aspects);
    }
    
    /**
     * Unobserves the given aspects independently of a particular instance so that the given observer is no longer notified on change.
     * 
     * @param observer the observer no longer to be notified.
     * @param aspects the aspects to be unobserved.
     * 
     * @require aspects.length > 0 : "At least one aspect is provided.";
     */
    public static void unobserveAspects(@Nonnull Observer observer, @Nonnull Aspect... aspects) {
        unobserve(aspectObservers, observer, aspects);
    }
    
    
    /**
     * Stores the observers that observe an aspect of this concept.
     */
    private @Nullable Map<Aspect, Set<Observer>> conceptObservers = null;
    
    /**
     * Observes the given aspects of this concept and notifies the given observer on change.
     * 
     * @param observer the observer to be notified.
     * @param aspects the aspects to be observed.
     * 
     * @require aspects.length > 0 : "At least one aspect is provided.";
     */
    public final void observe(@Nonnull Observer observer, @Nonnull Aspect... aspects) {
        if (conceptObservers == null) conceptObservers = new HashMap<Aspect, Set<Observer>>();
        observe(conceptObservers, observer, aspects);
    }
    
    /**
     * Unobserves the given aspects of this concept so that the given observer is no longer notified on change.
     * 
     * @param observer the observer no longer to be notified.
     * @param aspects the aspects to be unobserved.
     * 
     * @require aspects.length > 0 : "At least one aspect is provided.";
     */
    public final void unobserve(@Nonnull Observer observer, @Nonnull Aspect... aspects) {
        if (conceptObservers != null) unobserve(conceptObservers, observer, aspects);
    }
    
    /**
     * Notifies all the observers that observe the given aspect – both of this concept and independently of any concept.
     * Please note that the notification mechanism is only enabled for concepts on the client-side.
     * 
     * @param aspect the aspect to notify the registered observers about.
     */
    protected final void notify(@Nonnull Aspect aspect) {
        if (entity instanceof ClientEntity) {
            if (conceptObservers != null) notify(conceptObservers, aspect, this);
            notify(aspectObservers, aspect, this);
        }
    }
    
}
