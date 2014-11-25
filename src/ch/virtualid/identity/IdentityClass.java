package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a virtual identity, which can change identifiers and hosts.
 * Note that instances of this class are not necessarily unique (e.g. after identities have been merged).
 * 
 * @see HostIdentity
 * @see NonHostIdentity
 * 
 * @see Mapper
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class IdentityClass implements Identity, Immutable, SQLizable {
    
    /**
     * Stores the internal number that represents and indexes this identity.
     * The number remains the same after relocation but changes after merging.
     */
    private volatile long number;
    
    /**
     * Creates a new identity with the given internal number.
     * 
     * @param number the number that represents this identity.
     */
    IdentityClass(long number) {
        this.number = number;
    }
    
    @Pure
    @Override
    public final long getNumber() {
        return number;
    }
    
    /**
     * Sets the number that represents this identity.
     * 
     * @param number the new number of this identity.
     */
    final void setNumber(long number) {
        this.number = number;
    }
    
    
    @Pure
    @Override
    public final @Nonnull Block toBlock(@Nonnull SemanticType type) {
        assert type.isBasedOn(Identity.IDENTIFIER) : "The type is based on an identifier.";
        
        return getAddress().toBlock().setType(type);
    }
    
    @Pure
    @Override
    public final @Nonnull Blockable toBlockable(@Nonnull SemanticType type) {
        return toBlock(type).toBlockable();
    }
    
    
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
    public static @Nullable Identity get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final long number = resultSet.getLong(columnIndex);
        if (resultSet.wasNull()) return null;
        else return Mapper.getIdentity(number);
    }
    
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
    public static @Nonnull Identity getNotNull(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return Mapper.getIdentity(resultSet.getLong(columnIndex));
    }
    
    @Override
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, number);
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given identity.
     * 
     * @param identity the identity to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    public static void set(@Nullable Identity identity, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        if (identity == null) preparedStatement.setNull(parameterIndex, Types.BIGINT);
        else identity.set(preparedStatement, parameterIndex);
    }
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof IdentityClass)) return false;
        final @Nonnull IdentityClass other = (IdentityClass) object;
        return this.number == other.number;
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
    
}
