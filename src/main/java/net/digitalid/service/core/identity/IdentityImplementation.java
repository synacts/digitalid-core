package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class models a digital identity, which can change identifiers and hosts.
 * Note that instances of this class are not necessarily unique (e.g. after identities have been merged).
 * 
 * @see HostIdentity
 * @see NonHostIdentity
 * 
 * @see Mapper
 */
@Immutable
public abstract class IdentityImplementation implements Identity {
    
    /* -------------------------------------------------- Database ID -------------------------------------------------- */
    
    /**
     * Stores the database ID that represents and indexes this identity.
     * The database ID remains the same after relocation but changes after merging.
     */
    private volatile long databaseID;
    
    @Pure
    @Override
    public final long getDatabaseID() {
        return databaseID;
    }
    
    /**
     * Sets the database ID that represents this identity.
     * 
     * @param databaseID the new database ID of this identity.
     */
    final void setDatabaseID(long databaseID) {
        this.databaseID = databaseID;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new identity with the given database ID.
     * 
     * @param databaseID the database ID that represents this identity.
     */
    IdentityImplementation(long databaseID) {
        this.databaseID = databaseID;
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof IdentityImplementation)) return false;
        final @Nonnull IdentityImplementation other = (IdentityImplementation) object;
        return this.databaseID == other.databaseID;
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return (int) (databaseID ^ (databaseID >>> 32));
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return String.valueOf(databaseID);
    }
    
    /* -------------------------------------------------- Casting -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull InternalIdentity toInternalIdentity() throws InvalidEncodingException {
        if (this instanceof InternalIdentity) return (InternalIdentity) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to InternalIdentity.");
    }
    
    @Pure
    @Override
    public final @Nonnull ExternalIdentity toExternalIdentity() throws InvalidEncodingException {
        if (this instanceof ExternalIdentity) return (ExternalIdentity) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to ExternalIdentity.");
    }
    
    
    @Pure
    @Override
    public final @Nonnull HostIdentity toHostIdentity() throws InvalidEncodingException {
        if (this instanceof HostIdentity) return (HostIdentity) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to HostIdentity.");
    }
    
    @Pure
    @Override
    public final @Nonnull NonHostIdentity toNonHostIdentity() throws InvalidEncodingException {
        if (this instanceof NonHostIdentity) return (NonHostIdentity) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to NonHostIdentity.");
    }
    
    @Pure
    @Override
    public final @Nonnull InternalNonHostIdentity toInternalNonHostIdentity() throws InvalidEncodingException {
        if (this instanceof InternalNonHostIdentity) return (InternalNonHostIdentity) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to InternalNonHostIdentity.");
    }
    
    @Pure
    @Override
    public final @Nonnull Type toType() throws InvalidEncodingException {
        if (this instanceof Type) return (Type) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to Type.");
    }
    
    @Pure
    @Override
    public final @Nonnull SyntacticType toSyntacticType() throws InvalidEncodingException {
        if (this instanceof SyntacticType) return (SyntacticType) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to SyntacticType.");
    }
    
    @Pure
    @Override
    public final @Nonnull SemanticType toSemanticType() throws InvalidEncodingException {
        if (this instanceof SemanticType) return (SemanticType) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to SemanticType.");
    }
    
    
    @Pure
    @Override
    public final @Nonnull Person toPerson() throws InvalidEncodingException {
        if (this instanceof Person) return (Person) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to Person.");
    }
    
    @Pure
    @Override
    public final @Nonnull InternalPerson toInternalPerson() throws InvalidEncodingException {
        if (this instanceof InternalPerson) return (InternalPerson) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to InternalPerson.");
    }
    
    @Pure
    @Override
    public final @Nonnull NaturalPerson toNaturalPerson() throws InvalidEncodingException {
        if (this instanceof NaturalPerson) return (NaturalPerson) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to NaturalPerson.");
    }
    
    @Pure
    @Override
    public final @Nonnull ArtificialPerson toArtificialPerson() throws InvalidEncodingException {
        if (this instanceof ArtificialPerson) return (ArtificialPerson) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to ArtificialPerson.");
    }
    
    @Pure
    @Override
    public final @Nonnull ExternalPerson toExternalPerson() throws InvalidEncodingException {
        if (this instanceof ExternalPerson) return (ExternalPerson) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to ExternalPerson.");
    }
    
    @Pure
    @Override
    public final @Nonnull EmailPerson toEmailPerson() throws InvalidEncodingException {
        if (this instanceof EmailPerson) return (EmailPerson) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to EmailPerson.");
    }
    
    @Pure
    @Override
    public final @Nonnull MobilePerson toMobilePerson() throws InvalidEncodingException {
        if (this instanceof MobilePerson) return (MobilePerson) this;
        throw new InvalidEncodingException("" + getAddress() + " is a " + this.getClass().getSimpleName() + " and cannot be cast to MobilePerson.");
    }
    
    /* -------------------------------------------------- XDF -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull XDFConverter<Identity> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull SQLConverter<Identity> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
}
