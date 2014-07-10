package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.StringWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the concept of a virtual identity, which can change identifiers and hosts.
 * Note that instances of this class are not necessarily unique (e.g. after identities have been merged).
 * 
 * @see HostIdentity
 * @see NonHostIdentity
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Identity implements Immutable, SQLizable {
    
    /**
     * Stores the semantic type {@code @virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("@virtualid.ch").load(StringWrapper.TYPE);
    
    
    /**
     * Stores the internal number that represents and indexes this identity.
     * This number remains the same after relocation but changes after merging.
     */
    protected volatile long number;
    
    /**
     * Stores the presumable address of this identity.
     * This identifier is updated when the identity is relocated or merged.
     */
    protected @Nonnull Identifier address;
    
    /**
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    Identity(long number, @Nonnull Identifier address) {
        this.number = number;
        this.address = address;
    }
    
    @Pure
    public final long getNumber() {
        return number;
    }
    
    /**
     * Returns the address of this identity.
     * 
     * @return the address of this identity.
     */
    @Pure
    public final @Nonnull Identifier getAddress() {
        return address;
    }
    
    
    /**
     * Returns the category of this identity.
     * 
     * @return the category of this identity.
     */
    @Pure
    public abstract @Nonnull Category getCategory();
    
    /**
     * Returns whether this identity has been merged and updates the internal number and the identifier.
     * 
     * @return whether this identity has been merged.
     */
    public abstract boolean hasBeenMerged() throws SQLException, FailedIdentityException;
    
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     * 
     * @ensure !(result instanceof Type) || ((Type) result).isLoaded() : "If the result is a type, its declaration is loaded.";
     */
    @Pure
    public static @Nonnull Identity get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return Mapper.getIdentity(resultSet.getLong(columnIndex));
    }
    
    @Override
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, number);
    }
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Identity)) return false;
        final @Nonnull Identity other = (Identity) object;
        return number == other.number;
    }
    
    @Pure
    @Override
    public final int hashCode() {
        assert !(this instanceof Person) : "The hash code of persons may change and should thus not be used.";
        
        return (int) (number ^ (number >>> 32));
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return String.valueOf(number);
    }
    
    
    /**
     * Returns this identity as a {@link HostIdentity} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link HostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link HostIdentity}.
     */
    @Pure
    public final @Nonnull HostIdentity toHostIdentity() throws InvalidEncodingException {
        if (this instanceof HostIdentity) return (HostIdentity) this;
        throw new InvalidEncodingException("" + address + " is a " + this.getClass().getSimpleName() + " and cannot be cast to HostIdentity.");
    }
    
    /**
     * Returns this identity as a {@link NonHostIdentity} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link NonHostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link NonHostIdentity}.
     */
    @Pure
    public final @Nonnull NonHostIdentity toNonHostIdentity() throws InvalidEncodingException {
        if (this instanceof NonHostIdentity) return (NonHostIdentity) this;
        throw new InvalidEncodingException("" + address + " is a " + this.getClass().getSimpleName() + " and cannot be cast to NonHostIdentity.");
    }
    
    /**
     * Returns this identity as a {@link Type} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link Type}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link Type}.
     */
    @Pure
    public final @Nonnull Type toType() throws InvalidEncodingException {
        if (this instanceof Type) return (Type) this;
        throw new InvalidEncodingException("" + address + " is a " + this.getClass().getSimpleName() + " and cannot be cast to Type.");
    }
    
    /**
     * Returns this identity as a {@link SyntacticType} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link SyntacticType}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link SyntacticType}.
     */
    @Pure
    public final @Nonnull SyntacticType toSyntacticType() throws InvalidEncodingException {
        if (this instanceof SyntacticType) return (SyntacticType) this;
        throw new InvalidEncodingException("" + address + " is a " + this.getClass().getSimpleName() + " and cannot be cast to SyntacticType.");
    }
    
    /**
     * Returns this identity as a {@link SemanticType} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link SemanticType}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link SemanticType}.
     */
    @Pure
    public final @Nonnull SemanticType toSemanticType() throws InvalidEncodingException {
        if (this instanceof SemanticType) return (SemanticType) this;
        throw new InvalidEncodingException("" + address + " is a " + this.getClass().getSimpleName() + " and cannot be cast to SemanticType.");
    }
    
    /**
     * Returns this identity as a {@link Person} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link Person}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link Person}.
     */
    @Pure
    public final @Nonnull Person toPerson() throws InvalidEncodingException {
        if (this instanceof Person) return (Person) this;
        throw new InvalidEncodingException("" + address + " is a " + this.getClass().getSimpleName() + " and cannot be cast to Person.");
    }
    
    /**
     * Returns this identity as a {@link NaturalPerson} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link NaturalPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link NaturalPerson}.
     */
    @Pure
    public final @Nonnull NaturalPerson toNaturalPerson() throws InvalidEncodingException {
        if (this instanceof NaturalPerson) return (NaturalPerson) this;
        throw new InvalidEncodingException("" + address + " is a " + this.getClass().getSimpleName() + " and cannot be cast to NaturalPerson.");
    }
    
    /**
     * Returns this identity as a {@link ArtificialPerson} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link ArtificialPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link ArtificialPerson}.
     */
    @Pure
    public final @Nonnull ArtificialPerson toArtificialPerson() throws InvalidEncodingException {
        if (this instanceof ArtificialPerson) return (ArtificialPerson) this;
        throw new InvalidEncodingException("" + address + " is a " + this.getClass().getSimpleName() + " and cannot be cast to ArtificialPerson.");
    }
    
    /**
     * Returns this identity as a {@link EmailPerson} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link EmailPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link EmailPerson}.
     */
    @Pure
    public final @Nonnull EmailPerson toEmailPerson() throws InvalidEncodingException {
        if (this instanceof EmailPerson) return (EmailPerson) this;
        throw new InvalidEncodingException("" + address + " is a " + this.getClass().getSimpleName() + " and cannot be cast to EmailPerson.");
    }
    
}
