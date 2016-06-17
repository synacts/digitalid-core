package net.digitalid.core.identification.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.exceptions.state.value.CorruptParameterValueCombinationException;

import net.digitalid.core.conversion.NonRequestingConverters;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.conversion.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.NonRequestingXDFConverter;
import net.digitalid.core.identity.ExternalIdentity;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.Person;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.resolution.Category;
import net.digitalid.core.resolution.Mapper;

/**
 * This class models external identifiers.
 * 
 * @see EmailIdentifier
 * @see MobileIdentifier
 */
@Immutable
public abstract class ExternalIdentifier extends IdentifierImplementation implements NonHostIdentifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     *
     * @param string the string to check.
     * 
     * @return whether the given string conforms to the criteria of this class.
     */
    @Pure
    static boolean isConforming(@Nonnull String string) {
        return IdentifierImplementation.isConforming(string) && string.contains(":");
    }
    
    /**
     * Returns whether the given string is a valid identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        final int index = string.indexOf(':');
        if (index < 1) { return false; }
        final @Nonnull String scheme = string.substring(0, index);
        switch (scheme) {
            case "email": return EmailIdentifier.isValid(string);
            case "mobile": return MobileIdentifier.isValid(string);
            default: return false;
        }
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates an external identifier with the given string.
     * 
     * @param string the string of the external identifier.
     */
    ExternalIdentifier(@Nonnull @Validated String string) {
        super(string);
        
        Require.that(isValid(string)).orThrow("The string is a valid external identifier.");
    }
    
    /**
     * Returns a new external identifier with the given string.
     * 
     * @param string the string of the new external identifier.
     * 
     * @return a new external identifier with the given string.
     */
    @Pure
    public static @Nonnull Identifier get(@Nonnull @Validated String string) {
        final @Nonnull String scheme = string.substring(0, string.indexOf(':'));
        switch (scheme) {
            case "email": return EmailIdentifier.get(string);
            case "mobile": return MobileIdentifier.get(string);
            default: throw ShouldNeverHappenError.get("The scheme '" + scheme + "' is not valid.");
        }
    }
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public final @Nonnull Person getMappedIdentity() throws DatabaseException {
        Require.that(isMapped()).orThrow("This identifier is mapped.");
        
        final @Nonnull Identity identity = Mapper.getMappedIdentity(this);
        if (identity instanceof Person) { return (Person) identity; }
        else { throw CorruptParameterValueCombinationException.get("The mapped identity has a wrong type."); }
    }
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nonnull Person getIdentity() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    /**
     * Returns the category of this external identifier.
     * 
     * @return the category of this external identifier.
     * 
     * @ensure return.isExternalPerson() : "The returned category denotes an external person.";
     */
    @Pure
    public abstract @Nonnull Category getCategory();
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = Identifier.DECLARATION.renamedAs("external_identifier");
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<ExternalIdentifier> KEY_CONVERTER = new Identifier.StringConverter<>(ExternalIdentifier.class);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull NonRequestingXDFConverter<ExternalIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(ExternalIdentity.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<ExternalIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<ExternalIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
