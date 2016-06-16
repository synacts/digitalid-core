package net.digitalid.core.identifier;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

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

import net.digitalid.core.cache.exceptions.IdentityNotFoundException;
import net.digitalid.core.conversion.NonRequestingConverters;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.conversion.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.NonRequestingXDFConverter;
import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;
import net.digitalid.core.identity.EmailPerson;
import net.digitalid.core.identity.Person;
import net.digitalid.core.resolution.Category;
import net.digitalid.core.resolution.Mapper;

/**
 * This class models email identifiers.
 */
@Immutable
public final class EmailIdentifier extends ExternalIdentifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * The pattern that valid email identifiers have to match.
     */
    private static final Pattern pattern = Pattern.compile("email:[a-z0-9]+(?:[._-][a-z0-9]+)*@[a-z0-9]+(?:[.-][a-z0-9]+)*\\.[a-z][a-z]+");
    
    /**
     * Returns whether the given string is a valid email identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid email identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return ExternalIdentifier.isConforming(string) && pattern.matcher(string).matches();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates an email identifier with the given string.
     * 
     * @param string the string of the email identifier.
     */
    private EmailIdentifier(@Nonnull @Validated String string) {
        super(string);
        
        Require.that(isValid(string)).orThrow("The string is a valid email identifier.");
    }
    
    /**
     * Returns an email identifier with the given string.
     * 
     * @param string the string of the email identifier.
     * 
     * @return an email identifier with the given string.
     */
    @Pure
    public static @Nonnull EmailIdentifier get(@Nonnull @Validated String string) {
        return new EmailIdentifier(string);
    }
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Person getIdentity() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        if (!providerExists()) { throw IdentityNotFoundException.get(this); }
        return Mapper.getIdentity(this).castTo(Person.class);
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.EMAIL_PERSON;
    }
    
    /* -------------------------------------------------- Existence -------------------------------------------------- */
    
    /**
     * Returns the host of this email address.
     * 
     * @return the host of this email address.
     */
    @Pure
    public @Nonnull String getHost() {
        return getString().substring(getString().indexOf("@") + 1);
    }
    
    /**
     * Returns whether the provider of this email address exists.
     * 
     * @return whether the provider of this email address exists.
     */
    @Pure
    public boolean providerExists() {
        try {
            final @Nonnull InitialDirContext context = new InitialDirContext();
            final @Nonnull Attributes attributes = context.getAttributes("dns:/" + getHost(), new String[] {"MX"});
            return attributes.get("MX") != null;
        } catch (@Nonnull NamingException exception) {
            return false;
        }
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = Identifier.DECLARATION.renamedAs("email_identifier");
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<EmailIdentifier> KEY_CONVERTER = new Identifier.StringConverter<>(EmailIdentifier.class);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull NonRequestingXDFConverter<EmailIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(EmailPerson.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<EmailIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<EmailIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
