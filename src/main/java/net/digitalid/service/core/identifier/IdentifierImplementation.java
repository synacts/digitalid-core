package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

/**
 * This class models identifiers.
 * 
 * @see InternalIdentifier
 * @see ExternalIdentifier
 */
@Immutable
public abstract class IdentifierImplementation implements Identifier {
    
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
    public final boolean isMapped() throws AbortException {
        return Mapper.isMapped(this);
    }
    
    /* -------------------------------------------------- Casting to Non-Host Identifier -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull NonHostIdentifier toNonHostIdentifier() throws InvalidEncodingException {
        if (this instanceof NonHostIdentifier) return (NonHostIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to NonHostIdentifier.");
    }
    
    /* -------------------------------------------------- Casting to Internal Identifiers -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull InternalIdentifier toInternalIdentifier() throws InvalidEncodingException {
        if (this instanceof InternalIdentifier) return (InternalIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to InternalIdentifier.");
    }
    
    @Pure
    @Override
    public final @Nonnull HostIdentifier toHostIdentifier() throws InvalidEncodingException {
        if (this instanceof HostIdentifier) return (HostIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to HostIdentifier.");
    }
    
    @Pure
    @Override
    public final @Nonnull InternalNonHostIdentifier toInternalNonHostIdentifier() throws InvalidEncodingException {
        if (this instanceof InternalNonHostIdentifier) return (InternalNonHostIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to InternalNonHostIdentifier.");
    }
    
    /* -------------------------------------------------- Casting to External Identifiers -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull ExternalIdentifier toExternalIdentifier() throws InvalidEncodingException {
        if (this instanceof ExternalIdentifier) return (ExternalIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to ExternalIdentifier.");
    }
    
    @Pure
    @Override
    public final @Nonnull EmailIdentifier toEmailIdentifier() throws InvalidEncodingException {
        if (this instanceof EmailIdentifier) return (EmailIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to EmailIdentifier.");
    }
    
    @Pure
    @Override
    public final @Nonnull MobileIdentifier toMobileIdentifier() throws InvalidEncodingException {
        if (this instanceof MobileIdentifier) return (MobileIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to MobileIdentifier.");
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof IdentifierImplementation)) return false;
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
    
    /* -------------------------------------------------- XDF -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull AbstractNonRequestingXDFConverter<Identifier, Object> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull AbstractSQLConverter<Identifier, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
}
