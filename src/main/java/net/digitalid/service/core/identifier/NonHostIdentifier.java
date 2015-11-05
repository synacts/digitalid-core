package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.key.Caster;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.NonHostIdentity;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

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
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    public static final @Nonnull Caster<Identifier, NonHostIdentifier> CASTER = new Caster<Identifier, NonHostIdentifier>() {
        @Pure
        @Override
        protected @Nonnull NonHostIdentifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier.toNonHostIdentifier();
        }
    };
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<NonHostIdentifier> KEY_CONVERTER = new Identifier.StringConverter<>(CASTER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractNonRequestingXDFConverter<NonHostIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(NonHostIdentity.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<NonHostIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter("non_host_identifier"));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<NonHostIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
