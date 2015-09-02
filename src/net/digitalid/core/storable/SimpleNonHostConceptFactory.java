package net.digitalid.core.storable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.database.Column;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * This class is like {@link NonHostConceptFactory} except that the decoding of {@link Block blocks} throws less exceptions.
 * 
 * @see BlockBasedSimpleNonHostConceptFactory
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class SimpleNonHostConceptFactory<O> extends NonHostConceptFactory<O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public abstract @Nonnull O decodeNonNullable(@Nonnull NonHostEntity entity, @Nonnull @NonEncoding Block block) throws InvalidEncodingException;
    
    @Pure
    @Override
    public @Nullable O decodeNullable(@Nonnull NonHostEntity entity, @Nullable @NonEncoding Block block) throws InvalidEncodingException {
        if (block != null) return decodeNonNullable(entity, block);
        else return null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new simple non-concept factory with the given parameters.
     * 
     * @param type the semantic type that corresponds to the storable class.
     * @param columns the columns used to store objects of the storable class.
     */
    protected SimpleNonHostConceptFactory(@Nonnull @Loaded SemanticType type, @Nonnull @NonNullableElements Column... columns) {
        super(type, columns);
    }
    
}
