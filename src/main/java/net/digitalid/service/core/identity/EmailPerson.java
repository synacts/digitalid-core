package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.Int64Wrapper;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.key.Caster;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingXDFConverter;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identifier.EmailIdentifier;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identity.resolution.Category;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

/**
 * This class models an email person.
 */
@Immutable
public final class EmailPerson extends ExternalPerson {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code email.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("email.person@core.digitalid.net").load(ExternalPerson.IDENTIFIER);
    
    /* -------------------------------------------------- COnstructor -------------------------------------------------- */
    
    /**
     * Creates a new email person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the address of this email person.
     */
    EmailPerson(long number, @Nonnull EmailIdentifier address) {
        super(number, address);
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.EMAIL_PERSON;
    }
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identities to this subclass.
     */
    public static final @Nonnull Caster<Identity, EmailPerson> CASTER = new Caster<Identity, EmailPerson>() {
        @Pure
        @Override
        protected @Nonnull EmailPerson cast(@Nonnull Identity identity) throws InvalidEncodingException {
            return identity.toEmailPerson();
        }
    };
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractXDFConverter<EmailPerson, Object> XDF_CONVERTER = ChainingXDFConverter.get(new Identity.IdentifierConverter<>(CASTER), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<EmailPerson, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(CASTER), Int64Wrapper.getValueSQLConverter("email_person")); // TODO: Add GeneralReference.get("REFERENCES general_identity (identity) ON DELETE RESTRICT ON UPDATE RESTRICT")
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<EmailPerson, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
