package net.digitalid.service.core.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.internal.InternalException;
import net.digitalid.utility.validation.reference.NonCapturable;
import net.digitalid.utility.validation.state.Immutable;

import net.digitalid.database.core.exceptions.operation.FailedValueRestoringException;
import net.digitalid.database.core.exceptions.operation.FailedValueStoringException;
import net.digitalid.database.core.exceptions.state.value.CorruptValueException;

import net.digitalid.service.core.format.Format;
import net.digitalid.service.core.format.Tuple2Format;

@Immutable
public abstract class Tuple2Factory<O, E, O1, E1, O2, E2> extends Factory<O, E> {
    
    /* -------------------------------------------------- Factories -------------------------------------------------- */
    
    public final @Nonnull Factory<O1, E1> factory1;
    
    public final @Nonnull Factory<O2, E2> factory2;
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    protected Tuple2Factory(@Nonnull String name, @Nonnull Factory<O1, E1> factory1, @Nonnull Factory<O2, E2> factory2) {
        super(name);
        
        this.factory1 = factory1;
        this.factory2 = factory2;
    }
    
    /* -------------------------------------------------- Methods -------------------------------------------------- */
    
    @Override
    public final <R> R consume(@Nonnull O object, @NonCapturable @Nonnull Format<R> format) throws FailedValueStoringException, InternalException {
        final @Nonnull Tuple2Format<R, O1, E1, O2, E2> tuple2Format = format.getTuple2Format(this);
        consumeNonNullable(object, tuple2Format);
        return tuple2Format.finish();
    }
    
    public abstract void consumeNonNullable(@Nonnull O object, @NonCapturable @Nonnull Tuple2Format<?, O1, E1, O2, E2> format) throws FailedValueStoringException, InternalException;
    
    public abstract @Nullable O produceNullable(@Nonnull E external, @NonCapturable @Nonnull Tuple2Format<?, O1, E1, O2, E2> format) throws FailedValueRestoringException, CorruptValueException, InternalException;
    
}
