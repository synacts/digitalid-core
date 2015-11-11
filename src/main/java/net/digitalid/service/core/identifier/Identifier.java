package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.key.Caster;
import net.digitalid.service.core.converter.key.CastingNonRequestingKeyConverter;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.resolution.annotations.MappedRecipient;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.SQL;

/**
 * This interface models identifiers.
 * 
 * @see IdentifierImplementation
 * @see NonHostIdentifier
 */
@Immutable
public interface Identifier extends XDF<Identifier, Object>, SQL<Identifier, Object> {
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    /**
     * Returns the string of this identifier.
     * 
     * @return the string of this identifier.
     */
    @Pure
    public @Nonnull @Validated String getString();
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Casting to Non-Host Identifier -------------------------------------------------- */
    
    /**
     * Returns this identifier as a {@link NonHostIdentifier}.
     * 
     * @return this identifier as a {@link NonHostIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link NonHostIdentifier}.
     */
    @Pure
    public @Nonnull NonHostIdentifier toNonHostIdentifier() throws InvalidEncodingException;
    
    /* -------------------------------------------------- Casting to Internal Identifiers -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Casting to External Identifiers -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Key Converter -------------------------------------------------- */
    
    /**
     * This class allows to convert an identifier to its string and recover it again by downcasting the identifier returned by the overridden method with the given caster.
     */
    @Immutable
    public static final class StringConverter<I extends Identifier> extends CastingNonRequestingKeyConverter<I, Object, String, Object, Identifier> {
        
        /**
         * Creates a new identifier-string converter with the given caster.
         * 
         * @param caster the caster that allows to cast objects to the specified subtype.
         */
        protected StringConverter(@Nonnull Caster<Identifier, I> caster) {
            super(caster);
        }
        
        @Pure
        @Override
        public boolean isValid(@Nonnull String string) {
            return IdentifierImplementation.isValid(string);
        }
        
        @Pure
        @Override
        public @Nonnull String convert(@Nonnull I identifier) {
            return identifier.getString();
        }
        
        @Pure
        @Override
        public @Nonnull Identifier recoverSupertype(@Nonnull Object none, @Nonnull @Validated String string) throws InvalidEncodingException {
            return IdentifierImplementation.get(string);
        }
        
    }
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    public static final @Nonnull Caster<Identifier, Identifier> CASTER = new Caster<Identifier, Identifier>() {
        @Pure
        @Override
        protected @Nonnull Identifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier;
        }
    };
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<Identifier> KEY_CONVERTER = new Identifier.StringConverter<>(CASTER);
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractNonRequestingXDFConverter<Identifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(Identity.IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<Identifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter("identifier"));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<Identifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
