package net.digitalid.service.core.identifier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.factory.encoding.Encodable;
import net.digitalid.service.core.factory.encoding.AbstractNonRequestingEncodingFactory;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.identity.resolution.annotations.MappedRecipient;
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
import net.digitalid.utility.database.storing.Storable;

/**
 * This interface models identifiers.
 * 
 * @see IdentifierImplementation
 * @see NonHostIdentifier
 */
@Immutable
public interface Identifier extends Encodable<Identifier, Object>, Storable<Identifier, Object> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– String –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the string of this identifier.
     * 
     * @return the string of this identifier.
     */
    @Pure
    public @Nonnull @Validated String getString();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Mapping –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether this identifier is mapped.
     * 
     * @return whether this identifier is mapped.
     */
    @Pure
    @Locked
    @NonCommitting
    public boolean isMapped() throws AbortException;
    
    /**
     * Returns the mapped identity of this identifier.
     * 
     * @return the mapped identity of this identifier.
     */
    @Pure
    @Locked
    @NonCommitting
    @MappedRecipient
    public @Nonnull Identity getMappedIdentity() throws AbortException;
    
    /**
     * Returns the identity of this identifier.
     * 
     * @return the identity of this identifier.
     * 
     * @ensure !(result instanceof Type) || ((Type) result).isLoaded() : "If the result is a type, its declaration is loaded.";
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nonnull Identity getIdentity() throws AbortException, PacketException, ExternalException, NetworkException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Casting to Non-Host Identifier –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns this identifier as a {@link NonHostIdentifier}.
     * 
     * @return this identifier as a {@link NonHostIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link NonHostIdentifier}.
     */
    @Pure
    public @Nonnull NonHostIdentifier toNonHostIdentifier() throws InvalidEncodingException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Casting to Internal Identifiers –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns this identifier as an {@link InternalIdentifier}.
     * 
     * @return this identifier as an {@link InternalIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link InternalIdentifier}.
     */
    @Pure
    public @Nonnull InternalIdentifier toInternalIdentifier() throws InvalidEncodingException;
    
    /**
     * Returns this identifier as a {@link HostIdentifier}.
     * 
     * @return this identifier as a {@link HostIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link HostIdentifier}.
     */
    @Pure
    public @Nonnull HostIdentifier toHostIdentifier() throws InvalidEncodingException;
    
    /**
     * Returns this identifier as a {@link InternalNonHostIdentifier}.
     * 
     * @return this identifier as a {@link InternalNonHostIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link InternalNonHostIdentifier}.
     */
    @Pure
    public @Nonnull InternalNonHostIdentifier toInternalNonHostIdentifier() throws InvalidEncodingException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Casting to External Identifiers –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns this identifier as an {@link ExternalIdentifier}.
     * 
     * @return this identifier as an {@link ExternalIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link ExternalIdentifier}.
     */
    @Pure
    public @Nonnull ExternalIdentifier toExternalIdentifier() throws InvalidEncodingException;
    
    /**
     * Returns this identifier as an {@link EmailIdentifier}.
     * 
     * @return this identifier as an {@link EmailIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link EmailIdentifier}.
     */
    @Pure
    public @Nonnull EmailIdentifier toEmailIdentifier() throws InvalidEncodingException;
    
    /**
     * Returns this identifier as a {@link MobileIdentifier}.
     * 
     * @return this identifier as a {@link MobileIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link MobileIdentifier}.
     */
    @Pure
    public @Nonnull MobileIdentifier toMobileIdentifier() throws InvalidEncodingException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Caster –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * This class allows to cast identifiers to the right subclass.
     */
    @Immutable
    public static abstract class Caster<I extends Identifier> {
        
        /**
         * Casts the given identifier to the generic type.
         * 
         * @param identifier the identifier which is to be casted.
         * 
         * @return the given identifier casted to the generic type.
         * 
         * @throws InvalidEncodingException if the identifier is not an instance of the generic type.
         */
        @Pure
        protected abstract @Nonnull I cast(@Nonnull Identifier identifier) throws InvalidEncodingException;
        
        /**
         * Casts the given identifier to the generic type.
         * 
         * @param identifier the identifier which is to be casted.
         * 
         * @return the given identifier casted to the generic type.
         * 
         * @throws SQLException if the identifier is not an instance of the generic type.
         */
        @Pure
        final @Nonnull I castWithSQLException(@Nonnull Identifier identifier) throws SQLException {
            try {
                return cast(identifier);
            } catch (@Nonnull InvalidEncodingException exception) {
                throw new SQLException(exception);
            }
        }
        
    }
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    public static final @Nonnull Caster<Identifier> CASTER = new Caster<Identifier>() {
        @Pure
        @Override
        protected @Nonnull Identifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier;
        }
    };
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory<I extends Identifier> extends AbstractNonRequestingEncodingFactory<I, Object> {
        
        /**
         * Stores the caster that casts identifiers to the right subclass.
         */
        private final @Nonnull Caster<I> caster;
        
        /**
         * Creates a new encoding factory with the given type and caster.
         * 
         * @param type the semantic type that corresponds to the encodable class.
         * @param caster the caster that casts identifiers to the right subclass.
         */
        EncodingFactory(@Nonnull @BasedOn("@core.digitalid.net") SemanticType type, @Nonnull Caster<I> caster) {
            super(type);
            
            assert type.isBasedOn(Identity.IDENTIFIER) : "The given type is based on the identifier type.";
            
            this.caster = caster;
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull I identifier) {
            return StringWrapper.encodeNonNullable(getType(), identifier.getString());
        }
        
        @Pure
        @Override
        public @Nonnull I decodeNonNullable(@Nonnull Object none, @Nonnull @BasedOn("@core.digitalid.net") Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
            
            final @Nonnull String string = StringWrapper.decodeNonNullable(block);
            if (!IdentifierImplementation.isValid(string)) { throw new InvalidEncodingException("'" + string + "' is not a valid identifier."); }
            return caster.cast(IdentifierImplementation.get(string));
        }
        
    }
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull EncodingFactory<Identifier> ENCODING_FACTORY = new EncodingFactory<>(Identity.IDENTIFIER, CASTER);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The storing factory for this class.
     */
    @Immutable
    public static final class StoringFactory<I extends Identifier> extends AbstractStoringFactory<I, Object> {
        
        /**
         * Stores the caster that casts identifiers to the right subclass.
         */
        private final @Nonnull Caster<I> caster;
        
        /**
         * Creates a new storing factory with the given caster.
         * 
         * @param caster the caster that casts identifiers to the right subclass.
         */
        StoringFactory(@Nonnull Caster<I> caster) {
            super(Column.get("identifier", SQLType.VARCHAR));
            
            this.caster = caster;
        }
        
        @Pure
        @Override
        public @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull I identifier) {
            return FreezableArray.getNonNullable(identifier.getString());
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull I identifier, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setString(parameterIndex, identifier.getString());
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable I restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable String string = resultSet.getString(columnIndex);
            if (string == null) { return null; }
            if (!IdentifierImplementation.isValid(string)) { throw new SQLException("'" + string + "' is not a valid identifier."); }
            return caster.castWithSQLException(IdentifierImplementation.get(string));
        }
        
    }
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull StoringFactory<Identifier> STORING_FACTORY = new StoringFactory<>(CASTER);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Factories<Identifier, Object> FACTORIES = Factories.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
