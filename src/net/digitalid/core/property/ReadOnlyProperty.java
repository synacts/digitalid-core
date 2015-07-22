package net.digitalid.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;

/**
 * A property is an object that can be {@link PropertyObserver observed}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class ReadOnlyProperty<V, O extends PropertyObserver> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Validator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value validator of this property.
     */
    private final @Nonnull ValueValidator<? super V> validator;
    
    /**
     * Creates a new read-only property with the given validator.
     * 
     * @param validator the validator used to validate the value(s) of this property.
     */
    protected ReadOnlyProperty(@Nonnull ValueValidator<? super V> validator) {
        this.validator = validator;
    }
    
    /**
     * Returns the value validator of this property.
     * 
     * @return the value validator of this property.
     */
    @Pure
    public final @Nonnull ValueValidator<? super V> getValidator() {
        return validator;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Observers –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores null until the first observer registers and then the list of observers.
     */
    private @Nullable @NonFrozen FreezableList<O> observers;
    
    /**
     * Registers the given observer for this property.
     * 
     * @param observer the observer to be registered.
     */
    public final void register(@Nonnull O observer) {
        if (observers == null) observers = new FreezableLinkedList<>();
        observers.add(observer);
    }
    
    /**
     * Deregisters the given observer for this property.
     * 
     * @param observer the observer to be deregistered.
     */
    public final void deregister(@Nonnull O observer) {
        if (observers != null) observers.remove(observer);
    }
    
    /**
     * Returns whether the given observer is registered for this property.
     * 
     * @param observer the observer to check whether it is registered.
     * 
     * @return whether the given observer is registered for this property.
     */
    @Pure
    public final boolean isRegistered(@Nonnull O observer) {
        return observers != null && observers.contains(observer);
    }
    
    /**
     * Returns whether this property has observers.
     * 
     * @return whether this property has observers.
     */
    @Pure
    protected final boolean hasObservers() {
        return observers != null && !observers.isEmpty();
    }
    
    /**
     * Returns the observers of this property.
     * 
     * @return the observers of this property.
     */
    @Pure
    protected final @Nonnull @NonFrozen ReadOnlyList<O> getObservers() {
        if (observers == null) observers = new FreezableLinkedList<>();
        return observers;
    }
    
}
