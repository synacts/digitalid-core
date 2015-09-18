package net.digitalid.core.property.nonnullable;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.ConceptProperty;
import net.digitalid.core.property.ValueValidator;
import net.digitalid.core.synchronizer.Synchronizer;
import net.digitalid.core.tuples.ReadOnlyPair;

/**
 * This property belongs to a concept and stores a replaceable value that cannot be null.
 * 
 * @invariant (time == null) == (value == null) : "The time and value are either both null or both non-null.";
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class NonNullableConceptProperty<V, C extends Concept<C, E, ?>, E extends Entity> extends WriteableNonNullableProperty<V> implements ConceptProperty<C> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the concept to which this property belongs.
     */
    private final @Nonnull C concept;
    
    @Pure
    @Override
    public @Nonnull C getConcept() {
        return concept;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Table –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the table which stores the time and value of this property.
     */
    private final @Nonnull NonNullableConceptPropertyTable<V, C, E> table;
    
    @Pure
    @Override
    public @Nonnull NonNullableConceptPropertyTable<V, C, E> getTable() {
        return table;
    }
    
    /**
     * Loads the time and value of this property from the database.
     */
    @Pure
    @Locked
    @NonCommitting
    private void load() throws SQLException {
        final @Nonnull @NonNullableElements ReadOnlyPair<Time, V> pair = table.load(this);
        this.time = pair.getNonNullableElement0();
        this.value = pair.getNonNullableElement1();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new non-nullable concept property with the given parameters.
     * 
     * @param validator the validator used to validate the value of the new property.
     * @param concept the concept to which the new property belongs.
     * @param table the table which stores the time and value of the new property.
     */
    private NonNullableConceptProperty(@Nonnull ValueValidator<? super V> validator, @Nonnull C concept, @Nonnull NonNullableConceptPropertyTable<V, C, E> table) {
        super(validator);
        
        this.concept = concept;
        this.table = table;
        
        concept.register(this);
    }
    
    /**
     * Creates a new non-nullable concept property with the given parameters.
     * 
     * @param validator the validator used to validate the value of the new property.
     * @param concept the concept to which the new property belongs.
     * @param table the table which stores the time and value of the new property.
     * 
     * @return a new non-nullable concept property with the given parameters.
     */
    @Pure
    public static @Nonnull <V, C extends Concept<C, E, ?>, E extends Entity> NonNullableConceptProperty<V, C, E> get(@Nonnull ValueValidator<? super V> validator, @Nonnull C concept, @Nonnull NonNullableConceptPropertyTable<V, C, E> table) {
        return new NonNullableConceptProperty<>(validator, concept, table);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Time –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the time of the last modification.
     */
    private @Nullable Time time;
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull Time getTime() throws SQLException {
        if (time == null) load();
        assert time != null;
        return time;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value of this property.
     */
    private @Nullable @Validated V value;
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull @Validated V get() throws SQLException {
        if (value == null) load();
        assert value != null;
        return value;
    }
    
    @Locked
    @Override
    @Committing
    public void set(@Nonnull @Validated V newValue) throws SQLException {
        assert getValidator().isValid(newValue) : "The new value is valid.";
        
        final @Nonnull V oldValue = get();
        if (!newValue.equals(oldValue)) {
            Synchronizer.execute(new NonNullableConceptPropertyInternalAction(this, oldValue, newValue));
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Action –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    void replace(@Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull @Validated V oldValue, @Nonnull @Validated V newValue) throws SQLException {
        assert getValidator().isValid(oldValue) : "The old value is valid.";
        assert getValidator().isValid(newValue) : "The new value is valid.";
        
        table.replace(this, oldTime, newTime, oldValue, newValue);
        this.time = newTime;
        this.value = newValue;
        notify(oldValue, newValue);
    }
    
    @Locked
    @Override
    @NonCommitting
    public void reset() throws SQLException {
        if (hasObservers() && value != null) {
            final @Nonnull V oldValue = value;
            this.value = null;
            final @Nonnull V newValue = get();
            if (!oldValue.equals(newValue)) notify(oldValue, newValue);
        } else {
            this.time = null;
            this.value = null;
        }
    }
    
}
