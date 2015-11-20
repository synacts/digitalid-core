package net.digitalid.service.core.identity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.wrappers.Int64Wrapper;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingXDFConverter;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.service.core.identity.resolution.Successor;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.exceptions.DatabaseException;

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
    public static final @Nonnull AbstractXDFConverter<Person, Object> XDF_CONVERTER = ChainingXDFConverter.get(new Identity.IdentifierConverter<>(Person.class), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("person", true);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<Person, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(Person.class), Int64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Person, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
