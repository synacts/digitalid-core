package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.NonHostIdentity;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.declaration.ColumnDeclaration;
import net.digitalid.utility.database.exceptions.DatabaseException;
import net.digitalid.utility.system.exceptions.InternalException;

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
    public @Nonnull NonHostIdentity getMappedIdentity() throws DatabaseException;
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull NonHostIdentity getIdentity() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = Identifier.DECLARATION.renamedAs("non_host_identifier");
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<NonHostIdentifier> KEY_CONVERTER = new Identifier.StringConverter<>(NonHostIdentifier.class);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractNonRequestingXDFConverter<NonHostIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(NonHostIdentity.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<NonHostIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<NonHostIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
