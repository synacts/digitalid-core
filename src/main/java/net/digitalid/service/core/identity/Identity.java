package net.digitalid.service.core.identity;

import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.service.core.identity.resolution.Category;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.factory.encoding.AbstractEncodingFactory;
import net.digitalid.service.core.factory.encoding.Encodable;
import net.digitalid.service.core.factory.encoding.Encode;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.GeneralReference;
import net.digitalid.utility.database.column.SQLType;
import net.digitalid.utility.database.storing.AbstractStoringFactory;
import net.digitalid.utility.database.storing.Storable;

/**
 * This interface models a digital identity.
 * 
 * @see IdentityImplementation
 * @see InternalIdentity
 * @see ExternalIdentity
 */
@Immutable
public interface Identity extends Encodable<Identity, Object>, Storable<Identity, Object> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code @core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.IDENTITY_IDENTIFIER;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Database ID –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the database ID of this identity.
     * 
     * @return the database ID of this identity.
     */
    @Pure
    public long getDatabaseID();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Address –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the address of this identity.
     * 
     * @return the address of this identity.
     */
    @Pure
    public @Nonnull Identifier getAddress();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Category –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the category of this identity.
     * 
     * @return the category of this identity.
     */
    @Pure
    public @Nonnull Category getCategory();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Merging –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether this identity has been merged and updates the internal number and the identifier.
     * 
     * @param exception the exception to be rethrown if this identity has not been merged.
     * 
     * @return whether this identity has been merged.
     */
    @NonCommitting
    public boolean hasBeenMerged(@Nonnull SQLException exception) throws AbortException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Casting –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns this identity as an {@link InternalIdentity}.
     * 
     * @return this identity as an {@link InternalIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link InternalIdentity}.
     */
    @Pure
    public @Nonnull InternalIdentity toInternalIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link ExternalIdentity}.
     * 
     * @return this identity as an {@link ExternalIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link ExternalIdentity}.
     */
    @Pure
    public @Nonnull ExternalIdentity toExternalIdentity() throws InvalidEncodingException;
    
    
    /**
     * Returns this identity as a {@link HostIdentity}.
     * 
     * @return this identity as a {@link HostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link HostIdentity}.
     */
    @Pure
    public @Nonnull HostIdentity toHostIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link NonHostIdentity}.
     * 
     * @return this identity as a {@link NonHostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link NonHostIdentity}.
     */
    @Pure
    public @Nonnull NonHostIdentity toNonHostIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link InternalNonHostIdentity}.
     * 
     * @return this identity as an {@link InternalNonHostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link InternalNonHostIdentity}.
     */
    @Pure
    public @Nonnull InternalNonHostIdentity toInternalNonHostIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link Type}.
     * 
     * @return this identity as a {@link Type}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link Type}.
     */
    @Pure
    public @Nonnull Type toType() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link SyntacticType}.
     * 
     * @return this identity as a {@link SyntacticType}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link SyntacticType}.
     */
    @Pure
    public @Nonnull SyntacticType toSyntacticType() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link SemanticType}.
     * 
     * @return this identity as a {@link SemanticType}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link SemanticType}.
     */
    @Pure
    public @Nonnull SemanticType toSemanticType() throws InvalidEncodingException;
    
    
    /**
     * Returns this identity as a {@link Person}.
     * 
     * @return this identity as a {@link Person}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link Person}.
     */
    @Pure
    public @Nonnull Person toPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link InternalPerson}.
     * 
     * @return this identity as an {@link InternalPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link InternalPerson}.
     */
    @Pure
    public @Nonnull InternalPerson toInternalPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link NaturalPerson}.
     * 
     * @return this identity as a {@link NaturalPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link NaturalPerson}.
     */
    @Pure
    public @Nonnull NaturalPerson toNaturalPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link ArtificialPerson}.
     * 
     * @return this identity as a {@link ArtificialPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link ArtificialPerson}.
     */
    @Pure
    public @Nonnull ArtificialPerson toArtificialPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link ExternalPerson}.
     * 
     * @return this identity as an {@link ExternalPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link ExternalPerson}.
     */
    @Pure
    public @Nonnull ExternalPerson toExternalPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link EmailPerson}.
     * 
     * @return this identity as an {@link EmailPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link EmailPerson}.
     */
    @Pure
    public @Nonnull EmailPerson toEmailPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link MobilePerson}.
     * 
     * @return this identity as a {@link MobilePerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link MobilePerson}.
     */
    @Pure
    public @Nonnull MobilePerson toMobilePerson() throws InvalidEncodingException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Caster –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * This class allows to cast identities to the right subclass.
     */
    @Immutable
    public static abstract class Caster<I extends Identity> {
        
        /**
         * Casts the given identity to the generic type.
         * 
         * @param identity the identity which is to be casted.
         * 
         * @return the given identity casted to the generic type.
         * 
         * @throws InvalidEncodingException if the identity is not an instance of the generic type.
         */
        @Pure
        protected abstract @Nonnull I cast(@Nonnull Identity identity) throws InvalidEncodingException;
        
        /**
         * Casts the given identity to the generic type.
         * 
         * @param identity the identity which is to be casted.
         * 
         * @return the given identity casted to the generic type.
         * 
         * @throws SQLException if the identity is not an instance of the generic type.
         */
        @Pure
        final @Nonnull I castWithSQLException(@Nonnull Identity identity) throws SQLException {
            try {
                return cast(identity);
            } catch (@Nonnull InvalidEncodingException exception) {
                throw new SQLException(exception);
            }
        }
        
    }
    
    /**
     * Stores the caster that casts identities to this subclass.
     */
    public static final @Nonnull Caster<Identity> CASTER = new Caster<Identity>() {
        @Pure
        @Override
        protected @Nonnull Identity cast(@Nonnull Identity identity) throws InvalidEncodingException {
            return identity;
        }
    };
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory<I extends Identity> extends AbstractEncodingFactory<I, Object> {
        
        /**
         * Stores the caster that casts identities to the right subclass.
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
        public @Nonnull Block encodeNonNullable(@Nonnull I identity) {
            return Encode.nonNullable(identity.getAddress(), getType());
        }
        
        @Pure
        @Override
        public @Nonnull I decodeNonNullable(@Nonnull Object none, @Nonnull @BasedOn("@core.digitalid.net") Block block) throws AbortException, PacketException, ExternalException, NetworkException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
            
            return caster.cast(Identifier.ENCODING_FACTORY.decodeNonNullable(none, block).getIdentity());
        }
        
    }
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull EncodingFactory<Identity> ENCODING_FACTORY = new EncodingFactory<>(Identity.IDENTIFIER, CASTER);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The storing factory for this class.
     */
    @Immutable
    public static final class StoringFactory<I extends Identity> extends AbstractStoringFactory<I, Object> {
        
        /**
         * Stores the column of this storing factory.
         */
        private static final @Nonnull Column COLUMN = Column.get("identity", SQLType.BIGINT, false, GeneralReference.get("REFERENCES general_identity (identity) ON DELETE RESTRICT ON UPDATE RESTRICT"));
        
        /**
         * Stores the caster that casts identities to the right subclass.
         */
        private final @Nonnull Caster<I> caster;
        
        /**
         * Creates a new storing factory with the given caster.
         * 
         * @param caster the caster that casts identities to the right subclass.
         */
        StoringFactory(@Nonnull Caster<I> caster) {
            super(COLUMN);
            
            this.caster = caster;
        }
        
        @Pure
        @Override
        public @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull I identity) {
            return FreezableArray.getNonNullable(String.valueOf(identity.getDatabaseID()));
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull I identity, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setLong(parameterIndex, identity.getDatabaseID());
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable I restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final long databaseID = resultSet.getLong(columnIndex);
            return resultSet.wasNull() ? null : caster.castWithSQLException(Mapper.getIdentity(databaseID));
        }
        
    }
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull StoringFactory<Identity> STORING_FACTORY = new StoringFactory<>(CASTER);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Factories<Identity, Object> FACTORIES = Factories.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
