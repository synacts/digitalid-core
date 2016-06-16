package net.digitalid.core.identifier;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.NonRequestingConverters;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.conversion.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.NonRequestingXDFConverter;
import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;
import net.digitalid.core.identity.MobilePerson;
import net.digitalid.core.identity.Person;
import net.digitalid.core.resolution.Category;
import net.digitalid.core.resolution.Mapper;

/**
 * This class models mobile identifiers.
 */
@Immutable
public final class MobileIdentifier extends ExternalIdentifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * The pattern that valid mobile identifiers have to match.
     */
    private static final Pattern pattern = Pattern.compile("mobile:[0-9]{8,15}");
    
    /**
     * Returns whether the given string is a valid mobile identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid mobile identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return ExternalIdentifier.isConforming(string) && pattern.matcher(string).matches();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a mobile identifier with the given string.
     * 
     * @param string the string of the mobile identifier.
     */
    private MobileIdentifier(@Nonnull @Validated String string) {
        super(string);
        
        Require.that(isValid(string)).orThrow("The string is a valid mobile identifier.");
    }
    
    /**
     * Returns a mobile identifier with the given string.
     * 
     * @param string the string of the mobile identifier.
     * 
     * @return a mobile identifier with the given string.
     */
    @Pure
    public static @Nonnull MobileIdentifier get(@Nonnull @Validated String string) {
        return new MobileIdentifier(string);
    }
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Person getIdentity() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        return Mapper.getIdentity(this).castTo(Person.class);
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.MOBILE_PERSON;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = Identifier.DECLARATION.renamedAs("mobile_identifier");
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<MobileIdentifier> KEY_CONVERTER = new Identifier.StringConverter<>(MobileIdentifier.class);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull NonRequestingXDFConverter<MobileIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(MobilePerson.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<MobileIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<MobileIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
