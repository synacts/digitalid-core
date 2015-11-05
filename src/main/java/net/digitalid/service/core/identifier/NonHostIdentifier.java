package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.converter.Converters;
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
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull NonHostIdentity getMappedIdentity() throws AbortException;
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull NonHostIdentity getIdentity() throws AbortException, PacketException, ExternalException, NetworkException;
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    public static final @Nonnull IdentifierImplementation.Caster<NonHostIdentifier> CASTER = new IdentifierImplementation.Caster<NonHostIdentifier>() {
        @Pure
        @Override
        protected @Nonnull NonHostIdentifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier.toNonHostIdentifier();
        }
    };
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull IdentifierImplementation.XDFConverter<NonHostIdentifier> XDF_CONVERTER = new IdentifierImplementation.XDFConverter<>(NonHostIdentity.IDENTIFIER, CASTER);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull IdentifierImplementation.SQLConverter<NonHostIdentifier> SQL_CONVERTER = new IdentifierImplementation.SQLConverter<>(CASTER);
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<NonHostIdentifier, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
