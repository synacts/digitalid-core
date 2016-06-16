package net.digitalid.core.identity;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.Converters;
import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.core.conversion.xdf.ChainingRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.RequestingXDFConverter;
import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.resolution.Mapper;
import net.digitalid.core.resolution.Successor;

/**
 * This class models a person.
 * <p>
 * <em>Important:</em> Do not rely on the hash of persons because it may change at any time with mergers!
 * 
 * @see InternalPerson
 * @see ExternalPerson
 */
@Immutable
public abstract class Person extends NonHostIdentityImplementation {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new person with the given key.
     * 
     * @param key the number that represents this identity.
     */
    Person(long key) {
        super(key);
    }
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Sets the address of this person.
     * 
     * @param address the new address of this person.
     */
    public abstract void setAddress(@Nonnull Mapper.Key key, @Nonnull InternalNonHostIdentifier address);
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    @Override
    @NonCommitting
    public final boolean hasBeenMerged(@Nonnull SQLException exception) throws DatabaseException {
        final @Nullable InternalNonHostIdentifier successor = Successor.get(getAddress());
        if (successor != null && successor.isMapped()) {
            final @Nonnull InternalNonHostIdentity person = successor.getMappedIdentity();
            setAddress(person.getAddress());
            setKey(person.getKey());
            return true;
        } else {
            Mapper.unmap(this);
            throw exception;
        }
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("person@core.digitalid.net").load(NonHostIdentity.IDENTIFIER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull RequestingXDFConverter<Person, Object> XDF_CONVERTER = ChainingRequestingXDFConverter.get(new Identity.IdentifierConverter<>(Person.class), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("person", true);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<Person, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(Person.class), Integer64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Person, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
