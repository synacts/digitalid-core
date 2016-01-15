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
import net.digitalid.service.core.identifier.MobileIdentifier;
import net.digitalid.service.core.identity.resolution.Category;

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
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code mobile.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("mobile.person@core.digitalid.net").load(ExternalPerson.IDENTIFIER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull RequestingXDFConverter<MobilePerson, Object> XDF_CONVERTER = ChainingRequestingXDFConverter.get(new Identity.IdentifierConverter<>(MobilePerson.class), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("mobile_person", true);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<MobilePerson, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(MobilePerson.class), Integer64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<MobilePerson, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
