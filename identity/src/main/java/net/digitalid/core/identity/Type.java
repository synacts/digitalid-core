package net.digitalid.core.identity;

import java.sql.SQLException;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;

import net.digitalid.core.conversion.Converters;

import net.digitalid.core.conversion.xdf.ChainingRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.RequestingXDFConverter;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;

import net.digitalid.core.resolution.Mapper;

/**
 * This class models a type.
 * 
 * @see SyntacticType
 * @see SemanticType
 */
@Immutable
public abstract class Type extends NonHostIdentityImplementation implements InternalNonHostIdentity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Stores the presumable address of this type.
     * The address is updated when the type is relocated.
     */
    private @Nonnull InternalNonHostIdentifier address;
    
    @Pure
    @Override
    public final @Nonnull InternalNonHostIdentifier getAddress() {
        return address;
    }
    
    /**
     * Sets the address of this type.
     * 
     * @param address the new address of this type.
     */
    public final void setAddress(@Nonnull Mapper.Key key, @Nonnull InternalNonHostIdentifier address) {
        key.hashCode();
        this.address = address;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new type with the given key and address.
     * 
     * @param key the number that represents this identity.
     * @param address the current address of this identity.
     */
    Type(long key, @Nonnull InternalNonHostIdentifier address) {
        super(key);
        
        this.address = address;
    }
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public final boolean hasBeenMerged(@Nonnull SQLException exception) throws DatabaseException {
        Mapper.unmap(this);
        throw exception;
    }
    
    /* -------------------------------------------------- Loaded -------------------------------------------------- */
    
    /**
     * Stores whether the type declaration is loaded.
     * (Lazy loading is necessary for recursive type declarations.)
     */
    private boolean loaded = false;
    
    /**
     * Returns whether the type declaration is loaded.
     * 
     * @return whether the type declaration is loaded.
     */
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Sets the type declaration to already being loaded.
     * 
     * @ensure isLoaded() : "The type declaration is loaded.";
     */
    final void setLoaded() {
        loaded = true;
    }
    
    /**
     * Loads the type declaration from the cache or the network.
     * 
     * @require isNotLoaded() : "The type declaration is not loaded.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    @NonCommitting
    abstract void load() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
    /**
     * Ensures that the type declaration is loaded.
     * 
     * @ensure isLoaded() : "The type declaration is loaded.";
     */
    @NonCommitting
    public final void ensureLoaded() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        if (!loaded) { load(); }
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.TYPE_IDENTIFIER;
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull RequestingXDFConverter<Type, Object> XDF_CONVERTER = ChainingRequestingXDFConverter.get(new Identity.IdentifierConverter<>(Type.class), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("type", false);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<Type, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(Type.class), Integer64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Type, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
