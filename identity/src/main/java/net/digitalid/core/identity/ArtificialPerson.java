package net.digitalid.core.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQLConverter;

import net.digitalid.core.conversion.Converters;
import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.core.conversion.xdf.ChainingRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.RequestingXDFConverter;
import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.resolution.Category;

/**
 * This class models an artificial person.
 */
@Immutable
public final class ArtificialPerson extends InternalPerson {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new artificial person with the given key and address.
     * 
     * @param key the number that represents this identity.
     * @param address the current address of this identity.
     */
    ArtificialPerson(long key, @Nonnull InternalNonHostIdentifier address) {
        super(key, address);
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.ARTIFICIAL_PERSON;
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code artificial.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("artificial.person@core.digitalid.net").load(InternalPerson.IDENTIFIER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull RequestingXDFConverter<ArtificialPerson, Object> XDF_CONVERTER = ChainingRequestingXDFConverter.get(new Identity.IdentifierConverter<>(ArtificialPerson.class), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("artificial_person", true);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<ArtificialPerson, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(ArtificialPerson.class), Integer64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<ArtificialPerson, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}