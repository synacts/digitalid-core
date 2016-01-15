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
import net.digitalid.service.core.identifier.NonHostIdentifier;

/**
 * This interface models an external identity.
 * 
 * @see ExternalPerson
 */
@Immutable
public interface ExternalIdentity extends Identity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull NonHostIdentifier getAddress();
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code external@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("external@core.digitalid.net").load(Identity.IDENTIFIER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull RequestingXDFConverter<ExternalIdentity, Object> XDF_CONVERTER = ChainingRequestingXDFConverter.get(new Identity.IdentifierConverter<>(ExternalIdentity.class), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("external_identity", true);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<ExternalIdentity, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(ExternalIdentity.class), Integer64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<ExternalIdentity, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
