package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.Int64Wrapper;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.key.Caster;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingXDFConverter;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identifier.NonHostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

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
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identities to this subclass.
     */
    public static final @Nonnull Caster<Identity, ExternalIdentity> CASTER = new Caster<Identity, ExternalIdentity>() {
        @Pure
        @Override
        protected @Nonnull ExternalIdentity cast(@Nonnull Identity identity) throws InvalidEncodingException {
            return identity.toExternalIdentity();
        }
    };
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code external@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("external@core.digitalid.net").load(Identity.IDENTIFIER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractXDFConverter<ExternalIdentity, Object> XDF_CONVERTER = ChainingXDFConverter.get(new Identity.IdentifierConverter<>(CASTER), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("external_identity", true);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<ExternalIdentity, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(CASTER), Int64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<ExternalIdentity, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
