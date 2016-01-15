package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQLConverter;

import net.digitalid.service.core.block.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.xdf.ChainingRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.RequestingXDFConverter;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identifier.InternalIdentifier;

/**
 * This interface models an internal identity.
 * 
 * @see HostIdentity
 * @see InternalNonHostIdentity
 */
@Immutable
public interface InternalIdentity extends Identity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull InternalIdentifier getAddress();
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code internal@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("internal@core.digitalid.net").load(Identity.IDENTIFIER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull RequestingXDFConverter<InternalIdentity, Object> XDF_CONVERTER = ChainingRequestingXDFConverter.get(new Identity.IdentifierConverter<>(InternalIdentity.class), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("internal_identity", true);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<InternalIdentity, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(InternalIdentity.class), Integer64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<InternalIdentity, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
