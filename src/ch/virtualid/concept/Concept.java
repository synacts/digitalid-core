package ch.virtualid.concept;

import ch.virtualid.annotation.Pure;
import ch.virtualid.database.ClientEntity;
import ch.virtualid.database.Database;
import ch.virtualid.database.Entity;
import ch.xdf.Block;
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
 * @version 1.0
 */
public abstract class Concept /* extends BlockableObject implements Immutable */ {
    
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
     * Creates a new concept with the given block and entity.
     * 
     * @param block the block that encodes this concept.
     * @param entity the entity to which this concept belongs or null if it is impersonal.
     */
    protected Concept(@Nonnull Block block, @Nullable Entity entity) {
        super(block);
        
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
     * @param aspects the aspects to be observed.
     * @param observer the observer to be added.
     * 
     * @require !aspects.isEmpty() : "The set of aspects is not empty.";
     */
    private static void observe(@Nonnull Map<Aspect, Set<Observer>> map, @Nonnull Set<Aspect> aspects, @Nonnull Observer observer) {
        assert !aspects.isEmpty() : "The set of aspects is not empty.";
        
        for (@Nonnull Aspect aspect : aspects) {
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
     * @param aspects the aspects to be unobserved.
     * @param observer the observer to be removed.
     * 
     * @require !aspects.isEmpty() : "The set of aspects is not empty.";
     */
    private static void unobserve(@Nonnull Map<Aspect, Set<Observer>> map, @Nonnull Set<Aspect> aspects, @Nonnull Observer observer) {
        assert !aspects.isEmpty() : "The set of aspects is not empty.";
        
        for (@Nonnull Aspect aspect : aspects) {
            @Nullable Set<Observer> observers = map.get(aspect);
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
        @Nullable Set<Observer> observers = map.get(aspect);
        if (observers != null) {
            for (@Nonnull Observer observer : observers) observer.notify(aspect, concept);
        }
    }
    
    
    /**
     * Stores the observers that observe an aspect independently of any concept.
     */
    private static final @Nonnull Map<Aspect, Set<Observer>> aspectObservers = new HashMap<Aspect, Set<Observer>>();
    
    /**
     * Observes the given aspects independently of any concept and notifies the given observer on change.
     * 
     * @param aspects the aspects to be observed.
     * @param observer the observer to be notified.
     * 
     * @require !aspects.isEmpty() : "The set of aspects is not empty.";
     */
    public static final void observeAspects(@Nonnull Set<Aspect> aspects, @Nonnull Observer observer) {
        observe(aspectObservers, aspects, observer);
    }
    
    /**
     * Unobserves the given aspects independently of any concept so that the given observer is no longer notified on change.
     * 
     * @param aspects the aspects to be unobserved.
     * @param observer the observer no longer to be notified.
     * 
     * @require !aspects.isEmpty() : "The set of aspects is not empty.";
     */
    public static final void unobserveAspects(@Nonnull Set<Aspect> aspects, @Nonnull Observer observer) {
        unobserve(aspectObservers, aspects, observer);
    }
    
    
    /**
     * Stores the observers that observe an aspect of this concept.
     */
    private @Nullable Map<Aspect, Set<Observer>> conceptObservers = null;
    
    /**
     * Observes the given aspects of this concept and notifies the given observer on change.
     * 
     * @param aspects the aspects to be observed.
     * @param observer the observer to be notified.
     * 
     * @require !aspects.isEmpty() : "The set of aspects is not empty.";
     */
    public final void observe(@Nonnull Set<Aspect> aspects, @Nonnull Observer observer) {
        if (conceptObservers == null) conceptObservers = new HashMap<Aspect, Set<Observer>>();
        observe(conceptObservers, aspects, observer);
    }
    
    /**
     * Unobserves the given aspects of this concept so that the given observer is no longer notified on change.
     * 
     * @param aspects the aspects to be unobserved.
     * @param observer the observer no longer to be notified.
     * 
     * @require !aspects.isEmpty() : "The set of aspects is not empty.";
     */
    public final void unobserve(@Nonnull Set<Aspect> aspects, @Nonnull Observer observer) {
        if (conceptObservers != null) unobserve(conceptObservers, aspects, observer);
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
