package net.digitalid.service.core.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.property.extensible.ReadOnlyExtensibleProperty;
import net.digitalid.service.core.property.indexed.ReadOnlyIndexedProperty;
import net.digitalid.service.core.property.nonnullable.ReadOnlyNonNullableProperty;
import net.digitalid.service.core.property.nullable.ReadOnlyNullableProperty;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.system.logger.Log;

/**
 * A property is an object that can be {@link PropertyObserver observed}.
 * 
 * @see ReadOnlyNullableProperty
 * @see ReadOnlyNonNullableProperty
 * @see ReadOnlyExtensibleProperty
 * @see ReadOnlyIndexedProperty
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public abstract class ReadOnlyProperty<V, O extends PropertyObserver> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Validator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value validator used to validate the value(s) of this property.
     */
    private final @Nonnull ValueValidator<? super V> valueValidator;
    
    /**
     * Returns the value validator used to validate the value(s) of this property.
     * 
     * @return the value validator used to validate the value(s) of this property.
     */
    @Pure
    public final @Nonnull ValueValidator<? super V> getValueValidator() {
        return valueValidator;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new read-only property with the given value validator.
     * 
     * @param valueValidator the value validator used to validate the value(s) of this property.
     */
    protected ReadOnlyProperty(@Nonnull ValueValidator<? super V> valueValidator) {
        this.valueValidator = valueValidator;
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
        if (Database.isMultiAccess()) Log.warning("Since the database is in multi-access mode, you might not be notified about all changes.", new Exception());
        
        if (observers == null) observers = FreezableLinkedList.get();
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
        if (observers == null) observers = FreezableLinkedList.get();
        return observers;
    }
    
}
