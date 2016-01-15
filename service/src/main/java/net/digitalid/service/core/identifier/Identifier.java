package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.exceptions.internal.InternalException;
import net.digitalid.utility.system.castable.Castable;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.validation.state.Validated;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQL;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.service.core.block.wrappers.value.string.StringWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.key.CastingNonRequestingKeyConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.NonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.resolution.annotations.MappedRecipient;

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
    @NonCommitting
    public boolean isMapped() throws DatabaseException;
    
    /**
     * Returns the mapped identity of this identifier.
     * 
     * @return the mapped identity of this identifier.
     */
    @Pure
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
    @NonCommitting
    public @Nonnull Identity getIdentity() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
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
        public @Nonnull Identifier recoverSupertype(@Nonnull Object none, @Nonnull @Validated String string) {
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
    public static final @Nonnull NonRequestingXDFConverter<Identifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(Identity.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<Identifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<Identifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
