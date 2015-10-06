package net.digitalid.service.core.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.annotations.NonEncoding;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.reference.Captured;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.database.column.Column;

/**
 * This class is like {@link GlobalFactory} except that the decoding of {@link Block blocks} throws less exceptions.
 * The local factory allows only local information during {@link #decodeNonNullable(java.lang.Object, net.digitalid.service.core.wrappers.Block) decoding}.
 * 
 * @see BlockBasedLocalFactory
 * @see FactoryBasedLocalFactory
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class LocalFactory<O, E> extends GlobalFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public abstract @Nonnull O decodeNonNullable(@Nonnull E entity, @Nonnull @NonEncoding Block block) throws InvalidEncodingException;
    
    @Pure
    @Override
    public @Nullable O decodeNullable(@Nonnull E entity, @Nullable @NonEncoding Block block) throws InvalidEncodingException {
        if (block != null) return decodeNonNullable(entity, block);
        else return null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new simple factory with the given parameters.
     * 
     * @param type the semantic type that corresponds to the storable class.
     * @param columns the columns used to store objects of the storable class.
     */
    protected LocalFactory(@Nonnull @Loaded SemanticType type, @Captured @Nonnull @NonNullableElements Column... columns) {
        super(type, columns);
    }
    
}
