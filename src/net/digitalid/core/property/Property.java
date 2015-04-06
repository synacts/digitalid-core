package net.digitalid.core.property;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Description.
 * 
 * TODO: Also ReadOnlyOrderedProperty for things like subcontexts? Depends on their implementation.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public abstract class Property<O extends PropertyObserver> {
    
    private @Nullable List<O> observers;
    
    public final void register(@Nonnull O observer) {
        if (observers == null) observers = new LinkedList<>();
        observers.add(observer);
    }
    
    public final void deregister(@Nonnull O observer) {
        if (observers != null) observers.remove(observer);
    }
    
    public final boolean isRegistered(@Nonnull O observer) {
        return observers != null && observers.contains(observer);
    }
    
    protected final boolean hasObservers() {
        return observers != null && !observers.isEmpty();
    }
    
    protected final @Nonnull List<O> getObservers() {
        if (observers == null) observers = new LinkedList<>();
        return observers;
    }
    
}
