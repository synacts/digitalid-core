package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.castable.Castable;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.key.CastingNonRequestingKeyConverter;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
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
import net.digitalid.utility.database.declaration.ColumnDeclaration;
import net.digitalid.utility.database.exceptions.DatabaseException;

/**
 * This interface models identifiers.
 * 
 * @see IdentifierImplementation
 * @see NonHostIdentifier
 */
@Immutable
public interface Identifier extends Castable, XDF<Identifier, Object>, SQL<Identifier, Object> {
    
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
    public boolean isMapped() throws DatabaseException;
    
    /**
     * Returns the mapped identity of this identifier.
     * 
     * @return the mapped identity of this identifier.
     */
    @Pure
    @Locked
    @NonCommitting
    @MappedRecipient
    public @Nonnull Identity getMappedIdentity() throws DatabaseException;
    
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
    public @Nonnull Identity getIdentity() throws DatabaseException, PacketException, ExternalException, NetworkException;
    
    /* -------------------------------------------------- Key Converter -------------------------------------------------- */
    
    /**
     * This class allows to convert an identifier to its string and recover it again by downcasting the identifier returned by the overridden method to the given target class.
     */
    @Immutable
    public static final class StringConverter<I extends Identifier> extends CastingNonRequestingKeyConverter<I, Object, String, Object, Identifier> {
        
        /**
         * Creates a new identifier-string converter with the given target class.
         * 
         * @param targetClass the target class to which the recovered object is cast.
         */
        protected StringConverter(@Nonnull Class<I> targetClass) {
            super(targetClass);
        }
        
        @Pure
        @Override
        public boolean isValid(@Nonnull String string) {
            return IdentifierImplementation.isValid(string);
        }
        
        @Pure
        @Override
        public @Nonnull @Validated String convert(@Nonnull I identifier) {
            return identifier.getString();
        }
        
        @Pure
        @Override
        public @Nonnull Identifier recoverSupertype(@Nonnull Object none, @Nonnull @Validated String string) throws InvalidEncodingException {
            return IdentifierImplementation.get(string);
        }
        
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = ColumnDeclaration.get("identifier", StringWrapper.SQL_TYPE);
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<Identifier> KEY_CONVERTER = new Identifier.StringConverter<>(Identifier.class);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractNonRequestingXDFConverter<Identifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(Identity.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<Identifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<Identifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
