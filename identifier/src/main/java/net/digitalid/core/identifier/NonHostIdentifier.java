package net.digitalid.core.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;

import net.digitalid.core.conversion.NonRequestingConverters;

import net.digitalid.core.conversion.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.NonRequestingXDFConverter;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.identity.NonHostIdentity;

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
    public static final @Nonnull NonRequestingXDFConverter<NonHostIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(NonHostIdentity.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<NonHostIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<NonHostIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
