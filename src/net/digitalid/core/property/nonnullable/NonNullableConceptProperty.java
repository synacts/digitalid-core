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
import net.digitalid.core.property.ConceptProperty;
import net.digitalid.core.property.ValueValidator;
import net.digitalid.core.synchronizer.Synchronizer;
import net.digitalid.core.tuples.FreezablePair;

/**
 * This property belongs to a concept and stores a replaceable value that cannot be null.
 * 
 * @invariant (time == null) == (value == null) : "The time and value are either both null or both non-null.";
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class NonNullableConceptProperty<V, C extends Concept<C>> extends WriteableNonNullableProperty<V> implements ConceptProperty<C> {
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Database –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    private final @Nonnull NonNullableConceptPropertyTable<V> table;
    
    /**
     * 
     */
    @Pure
    @NonCommitting
    private void load() throws SQLException {
        final @Nonnull @NonNullableElements FreezablePair<Time, V> pair = table.load(this);
        this.time = pair.getNonNullableElement0();
        this.value = pair.getNonNullableElement1();
    }
    
    /**
     * 
     * 
     * @param oldValue
     * @param newValue
     * 
     * @require !oldValue.equals(newValue) : "The old and the new value are not the same.";
     */
    @NonCommitting
    void replace(@Nonnull Time oldTime, @Nonnull Time newTime, @Nonnull @Validated V oldValue, @Nonnull @Validated V newValue) throws SQLException {
        assert getValidator().isValid(oldValue) : "The old value is valid.";
        assert getValidator().isValid(newValue) : "The new value is valid.";
        
        table.replace(this, oldTime, newTime, oldValue, newValue);
        this.time = newTime;
        this.value = newValue;
        notify(oldValue, newValue);
    }
    
    /**
     * Resets the time and value of this property.
     */
    @Pure
    void reset() throws SQLException {
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    private NonNullableConceptProperty(@Nonnull ValueValidator<? super V> validator, @Nonnull C concept, @Nonnull NonNullableConceptPropertyTable<V> table) {
        super(validator);
        
        this.concept = concept;
        this.table = table;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Time –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the time of the last modification.
     */
    private @Nullable Time time;
    
    @Pure
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
    
}
