package net.digitalid.service.core.factory.encoding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class is like {@link AbstractEncodingFactory} except that the decoding of {@link Block blocks} throws less exceptions.
 * This factory does not allow file, network and database requests during {@link #decodeNonNullable(java.lang.Object, net.digitalid.service.core.block.Block) decoding}.
 */
@Immutable
public abstract class AbstractNonRequestingEncodingFactory<O, E> extends AbstractEncodingFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull AbstractNonRequestingEncodingFactory<O, E> setType(@Nonnull SemanticType type) {
        return SubtypingNonRequestingEncodingFactory.get(type, this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public abstract @Nonnull O decodeNonNullable(@Nonnull E entity, @Nonnull @NonEncoding Block block) throws InvalidEncodingException;
    
    @Pure
    @Override
    public final @Nullable O decodeNullable(@Nonnull E entity, @Nullable @NonEncoding Block block) throws InvalidEncodingException {
        return block == null ? null : decodeNonNullable(entity, block);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new abstract non-requesting encoding factory with the given type.
     * 
     * @param type the semantic type that corresponds to the encodable class.
     */
    protected AbstractNonRequestingEncodingFactory(@Nonnull @Loaded SemanticType type) {
        super(type);
    }
    
}
