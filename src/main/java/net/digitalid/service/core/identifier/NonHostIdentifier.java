package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.identity.NonHostIdentity;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This interface models non-host identifiers.
 * 
 * @see InternalNonHostIdentifier
 * @see ExternalIdentifier
 */
@Immutable
public interface NonHostIdentifier extends Identifier {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Mapping –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull NonHostIdentity getMappedIdentity() throws AbortException;
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull NonHostIdentity getIdentity() throws AbortException, PacketException, ExternalException, NetworkException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    public static final @Nonnull IdentifierClass.Caster<NonHostIdentifier> CASTER = new IdentifierClass.Caster<NonHostIdentifier>() {
        @Pure
        @Override
        protected @Nonnull NonHostIdentifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier.toNonHostIdentifier();
        }
    };
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull IdentifierClass.EncodingFactory<NonHostIdentifier> ENCODING_FACTORY = new IdentifierClass.EncodingFactory<>(NonHostIdentity.IDENTIFIER, CASTER);
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull IdentifierClass.StoringFactory<NonHostIdentifier> STORING_FACTORY = new IdentifierClass.StoringFactory<>(CASTER);
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Factories<NonHostIdentifier, Object> FACTORIES = Factories.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
