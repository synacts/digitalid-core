package net.digitalid.service.core.identity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.Int64Wrapper;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.key.Caster;
import net.digitalid.service.core.converter.key.CastingKeyConverter;
import net.digitalid.service.core.converter.key.CastingNonRequestingKeyConverter;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identity.resolution.Category;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.SQL;

/**
 * This interface models a digital identity.
 * 
 * @see IdentityImplementation
 * @see InternalIdentity
 * @see ExternalIdentity
 * 
 * @see Mapper
 */
@Immutable
public interface Identity extends XDF<Identity, Object>, SQL<Identity, Object> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code @core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.IDENTITY_IDENTIFIER;
    
    /* -------------------------------------------------- Database ID -------------------------------------------------- */
    
    /**
     * Returns the database ID of this identity.
     * 
     * @return the database ID of this identity.
     */
    @Pure
    public long getDatabaseID();
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Returns the address of this identity.
     * 
     * @return the address of this identity.
     */
    @Pure
    public @Nonnull Identifier getAddress();
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    /**
     * Returns the category of this identity.
     * 
     * @return the category of this identity.
     */
    @Pure
    public @Nonnull Category getCategory();
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    /**
     * Returns whether this identity has been merged and updates the internal number and the identifier.
     * 
     * @param exception the exception to be rethrown if this identity has not been merged.
     * 
     * @return whether this identity has been merged.
     */
    @NonCommitting
    public boolean hasBeenMerged(@Nonnull SQLException exception) throws AbortException;
    
    /* -------------------------------------------------- Casting to Internal vs. External Identity -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Casting to Host vs. Non-Host Identity -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Casting to Type -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Casting to Person -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Key Converters -------------------------------------------------- */
    
    /**
     * This class allows to convert an identity to its address and recover it again by downcasting the identity returned by the overridden method with the given caster.
     */
    @Immutable
    public static final class IdentifierConverter<I extends Identity> extends CastingKeyConverter<I, Object, Identifier, Identity> {
        
        /**
         * Creates a new identity-identifier converter with the given caster.
         * 
         * @param caster the caster that allows to cast objects to the specified subtype.
         */
        protected IdentifierConverter(@Nonnull Caster<Identity, I> caster) {
            super(caster);
        }
        
        @Pure
        @Override
        public @Nonnull Identifier convert(@Nonnull I identity) {
            return identity.getAddress();
        }
        
        @Pure
        @Override
        public @Nonnull Identity recoverSupertype(@Nonnull Object none, @Nonnull Identifier identifier) throws AbortException, PacketException, ExternalException, NetworkException {
            return identifier.getIdentity();
        }
        
    }
    
    /**
     * This class allows to convert an identity to its database ID and recover it again by downcasting the identity returned by the overridden method with the given caster.
     */
    @Immutable
    public static final class LongConverter<I extends Identity> extends CastingNonRequestingKeyConverter<I, Object, Long, Identity> {
        
        /**
         * Creates a new identity-identifier converter with the given caster.
         * 
         * @param caster the caster that allows to cast objects to the specified subtype.
         */
        protected LongConverter(@Nonnull Caster<Identity, I> caster) {
            super(caster);
        }
        
        @Pure
        @Override
        public @Nonnull Long convert(@Nonnull I identity) {
            return identity.getDatabaseID();
        }
        
        @Pure
        @Override
        public @Nonnull Identity recoverSupertype(@Nonnull Object none, @Nonnull Long databaseID) throws InvalidEncodingException {
            try {
                return Mapper.getIdentity(databaseID);
            } catch (@Nonnull AbortException exception) {
                throw new InvalidEncodingException(exception);
            }
        }
        
    }
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    public static final @Nonnull Caster<Identity, Identity> CASTER = new Caster<Identity, Identity>() {
        @Pure
        @Override
        protected @Nonnull Identity cast(@Nonnull Identity identity) throws InvalidEncodingException {
            return identity;
        }
    };
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractXDFConverter<Identity, Object> XDF_CONVERTER = ChainingXDFConverter.get(new Identity.IdentifierConverter<>(CASTER), Identifier.XDF_CONVERTER);
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<Identity, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(CASTER), Int64Wrapper.getValueSQLConverter("identity", Mapper.REFERENCE));
    
    // TODO: Introduce doAfterCreation([pass the other columns of the primary key?]) and doBeforeDeletion() methods in the AbstractSQLConverter, which can be used for Mapper.addReference and Mapper.removeReference and to create an index on the column in case of identities. (Update: Now just make use of this possibility.)
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Identity, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
