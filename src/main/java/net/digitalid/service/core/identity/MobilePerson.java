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
import net.digitalid.service.core.identifier.MobileIdentifier;
import net.digitalid.service.core.identity.resolution.Category;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

/**
 * This class models a mobile person.
 */
@Immutable
public final class MobilePerson extends ExternalPerson {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new mobile person with the given key and address.
     * 
     * @param key the number that represents this identity.
     * @param address the address of this mobile person.
     */
    MobilePerson(long key, @Nonnull MobileIdentifier address) {
        super(key, address);
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.MOBILE_PERSON;
    }
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identities to this subclass.
     */
    public static final @Nonnull Caster<Identity, MobilePerson> CASTER = new Caster<Identity, MobilePerson>() {
        @Pure
        @Override
        protected @Nonnull MobilePerson cast(@Nonnull Identity identity) throws InvalidEncodingException {
            return identity.toMobilePerson();
        }
    };
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code mobile.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("mobile.person@core.digitalid.net").load(ExternalPerson.IDENTIFIER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractXDFConverter<MobilePerson, Object> XDF_CONVERTER = ChainingXDFConverter.get(new Identity.IdentifierConverter<>(CASTER), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("mobile_person", true);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<MobilePerson, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(CASTER), Int64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<MobilePerson, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
