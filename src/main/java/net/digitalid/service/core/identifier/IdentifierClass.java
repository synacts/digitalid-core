package net.digitalid.service.core.identifier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.factory.encoding.NonRequestingEncodingFactory;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;
import net.digitalid.utility.database.storing.AbstractStoringFactory;

/**
 * This class models identifiers.
 * 
 * @see InternalIdentifier
 * @see ExternalIdentifier
 */
@Immutable
public abstract class IdentifierClass implements Identifier {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Validity –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– String –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the string of this identifier.
     */
    private final @Nonnull @Validated String string;
    
    @Pure
    @Override
    public final @Nonnull String getString() {
        return string;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates an identifier with the given string.
     * 
     * @param string the string of the identifier.
     */
    IdentifierClass(@Nonnull @Validated String string) {
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Mapping –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public final boolean isMapped() throws AbortException {
        return Mapper.isMapped(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Casting to Non-Host Identifier –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Nonnull NonHostIdentifier toNonHostIdentifier() throws InvalidEncodingException {
        if (this instanceof NonHostIdentifier) return (NonHostIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to NonHostIdentifier.");
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Casting to Internal Identifiers –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Casting to External Identifiers –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof IdentifierClass)) return false;
        final @Nonnull IdentifierClass other = (IdentifierClass) object;
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends NonRequestingEncodingFactory<Identifier, Object> {
        
        /**
         * Creates a new encoding factory.
         */
        private EncodingFactory() {
            super(Identity.IDENTIFIER);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull Identifier identifier) {
            return StringWrapper.encodeNonNullable(Identity.IDENTIFIER, identifier.getString());
        }
        
        @Pure
        @Override
        public @Nonnull Identifier decodeNonNullable(@Nonnull Object none, @Nonnull @BasedOn("@core.digitalid.net") Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
            
            final @Nonnull String string = StringWrapper.decodeNonNullable(block);
            if (!isValid(string)) throw new InvalidEncodingException("'" + string + "' is not a valid identifier.");
            return get(string);
        }
        
    }
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull EncodingFactory ENCODING_FACTORY = new EncodingFactory();
    
    @Pure
    @Override
    public final @Nonnull EncodingFactory getEncodingFactory() {
        return ENCODING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The storing factory for this class.
     */
    @Immutable
    public static final class StoringFactory extends AbstractStoringFactory<Identifier, Object> {
        
        /**
         * Creates a new storing factory.
         */
        private StoringFactory() {
            super(Column.get("identifier", SQLType.VARCHAR));
        }
        
        @Pure
        @Override
        public @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull Identifier identifier) {
            return FreezableArray.getNonNullable(identifier.getString());
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull Identifier identifier, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, identifier.getString());
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable Identifier restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable String string = resultSet.getString(columnIndex);
            if (string == null) { return null; }
            if (!isValid(string)) { throw new SQLException("'" + string + "' is not a valid identifier."); }
            return get(string);
        }
        
    }
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull StoringFactory STORING_FACTORY = new StoringFactory();
    
    @Pure
    @Override
    public final @Nonnull StoringFactory getStoringFactory() {
        return STORING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Factories<Identifier, Object> FACTORIES = Factories.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
