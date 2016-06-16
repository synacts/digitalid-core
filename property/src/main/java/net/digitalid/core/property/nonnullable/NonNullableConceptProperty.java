package net.digitalid.core.property.nonnullable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;

import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.property.ConceptProperty;
import net.digitalid.core.synchronizer.Synchronizer;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.property.nonnullable.WritableNonNullableProperty;

/**
 * This property belongs to a concept and stores a replaceable value that cannot be null.
 * 
 * @invariant (time == null) == (value == null) : "The time and value are either both null or both non-null.";
 */
public final class NonNullableConceptProperty<V, C extends Concept<C, E, ?>, E extends Entity> extends WritableNonNullableProperty<V> implements ConceptProperty<V, C, E> {
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    /**
     * Stores the property setup that contains the required information.
     */
    private final @Nonnull NonNullableConceptPropertySetup<V, C, E> propertySetup;
    
    public @Nonnull NonNullableConceptPropertySetup<V, C, E> getConceptPropertySetup() {
        return this.propertySetup;
    }
    
    /* -------------------------------------------------- Concept -------------------------------------------------- */
    
    /**
     * Stores the concept to which this property belongs.
     */
    private final @Nonnull C concept;
    
    @Pure
    @Override
    public @Nonnull C getConcept() {
        return concept;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new non-nullable concept property with the given parameters.
     * 
     * @param concept the concept to which the new property belongs.
     * @param propertySetup the property factory that contains the required information.
     */
    NonNullableConceptProperty(@Nonnull NonNullableConceptPropertySetup<V, C, E> propertySetup, @Nonnull C concept) {
        super(propertySetup.getValueValidator());
        
        this.propertySetup = propertySetup;
        this.concept = concept;
        
        concept.register(this);
    }
    
    /**
     * Creates and returns a new non-nullable concept property with the given parameters.
     * 
     * @param concept the concept to which the new property belongs.
     * @param propertySetup the property factory that contains the required information.
     * @return a new non-nullable concept property with the given parameters.
     */
    public static @Nonnull <V, C extends Concept<C, E, ?>, E extends Entity> NonNullableConceptProperty<V, C, E> get(@Nonnull NonNullableConceptPropertySetup<V, C, E> propertySetup, @Nonnull C concept) {
        return new NonNullableConceptProperty<>(propertySetup, concept);
    }
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    /**
     * Loads the time and value of this property from the database.
     */
    @Pure
    @Locked
    @NonCommitting
    private void load() throws DatabaseException {
        final @Nonnull @NonNullableElements ReadOnlyPair<Time, V> pair = propertySetup.getPropertyTable().load(this, propertySetup);
        this.time = pair.getNonNullableElement0();
        this.value = pair.getNonNullableElement1();
    }
    
    /* -------------------------------------------------- Time -------------------------------------------------- */
    
    /**
     * Stores the time of the last modification.
     */
    private @Nullable Time time;
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull Time getTime() throws DatabaseException {
        if (time == null) { load(); }
        assert time != null;
        return time;
    }
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Stores the value of this property.
     */
    private @Nullable @Validated V value;
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull @Validated V get() throws DatabaseException {
        if (value == null) { load(); }
        assert value != null;
        return value;
    }
    
    @Locked
    @Override
    @Committing
    public void set(@Nonnull @Validated V newValue) throws DatabaseException {
        Require.that(getValueValidator().isValid(newValue)).orThrow("The new value is valid.");
        
        final @Nonnull V oldValue = get();
        if (!newValue.equals(oldValue)) {
            Synchronizer.execute(NonNullableConceptPropertyInternalAction.get(this, oldValue, newValue));
        }
    }
    
    /* -------------------------------------------------- Action -------------------------------------------------- */
    
    /**
     * Replaces the time and value of this property from the {@link NonNullableConceptPropertyInternalAction}.
     * 
     * @param oldTime the old time of this property.
     * @param newTime the new time of this property.
     * @param oldValue the old value of this property.
     * @param newValue the new value of this property.
     * 
     * @require !oldValue.equals(newValue) : "The old and the new value are not the same.";
     */
    @Locked
    @NonCommitting
    void replace(@Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull @Validated V oldValue, @Nonnull @Validated V newValue) throws DatabaseException {
        Require.that(getValueValidator().isValid(oldValue)).orThrow("The old value is valid.");
        Require.that(getValueValidator().isValid(newValue)).orThrow("The new value is valid.");
        
        propertySetup.getPropertyTable().replace(this, oldTime, newTime, oldValue, newValue);
        this.time = newTime;
        this.value = newValue;
        notify(oldValue, newValue);
    }
    
    @Locked
    @Override
    @NonCommitting
    public void reset() throws DatabaseException {
        if (hasObservers() && value != null) {
            final @Nonnull V oldValue = value;
            this.value = null;
            final @Nonnull V newValue = get();
            if (!oldValue.equals(newValue)) { notify(oldValue, newValue); }
        } else {
            this.time = null;
            this.value = null;
        }
    }
    
}
