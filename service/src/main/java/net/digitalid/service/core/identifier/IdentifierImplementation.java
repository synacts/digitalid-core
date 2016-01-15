package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.system.castable.CastableObject;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.validation.state.Validated;

import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.service.core.converter.xdf.NonRequestingXDFConverter;
import net.digitalid.service.core.identity.resolution.Mapper;

/**
 * This class models identifiers.
 * 
 * @see InternalIdentifier
 * @see ExternalIdentifier
 */
@Immutable
public abstract class IdentifierImplementation extends CastableObject implements Identifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     * This method is called by the validity checkers of the subtypes to prevent infinite recursion.
     *
     * @param string the string to check.
     * 
     * @return whether the given string conforms to the criteria of this class.
     */
    @Pure
    static boolean isConforming(@Nonnull String string) {
        return string.length() < 64;
    }
    
    /**
     * Returns whether the given string is a valid identifier.
     * This method delegates the validation to the subtypes.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return string.contains(":") ? ExternalIdentifier.isValid(string) : InternalIdentifier.isValid(string);
    }
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    /**
     * Stores the string of this identifier.
     */
    private final @Nonnull @Validated String string;
    
    @Pure
    @Override
    public final @Nonnull String getString() {
        return string;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates an identifier with the given string.
     * 
     * @param string the string of the identifier.
     */
    IdentifierImplementation(@Nonnull @Validated String string) {
        assert isValid(string) : "The string is a valid identifier.";
        
        this.string = string;
    }
    
    /**
     * Returns a new identifier with the given string.
     * 
     * @param string the string of the new identifier.
     * 
     * @return a new identifier with the given string.
     */
    @Pure
    public static @Nonnull Identifier get(@Nonnull @Validated String string) {
        return string.contains(":") ? ExternalIdentifier.get(string) : InternalIdentifier.get(string);
    }
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public final boolean isMapped() throws DatabaseException {
        return Mapper.isMapped(this);
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof IdentifierImplementation)) { return false; }
        final @Nonnull IdentifierImplementation other = (IdentifierImplementation) object;
        return string.equals(other.string);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return string.hashCode();
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return "'" + string + "'";
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull NonRequestingXDFConverter<Identifier, Object> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull SQLConverter<Identifier, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
}
