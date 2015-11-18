package net.digitalid.service.core.identifier;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.key.Caster;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.IdentityNotFoundException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.EmailPerson;
import net.digitalid.service.core.identity.Person;
import net.digitalid.service.core.identity.resolution.Category;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.declaration.ColumnDeclaration;

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
        
        assert isValid(string) : "The string is a valid email identifier.";
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
    public @Nonnull Person getIdentity() throws AbortException, PacketException, ExternalException, NetworkException {
        if (!providerExists()) { throw new IdentityNotFoundException(this); }
        return Mapper.getIdentity(this).toPerson();
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
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    public static final @Nonnull Caster<Identifier, EmailIdentifier> CASTER = new Caster<Identifier, EmailIdentifier>() {
        @Pure
        @Override
        protected @Nonnull EmailIdentifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier.toEmailIdentifier();
        }
    };
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = Identifier.DECLARATION.renamedAs("email_identifier");
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<EmailIdentifier> KEY_CONVERTER = new Identifier.StringConverter<>(CASTER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractNonRequestingXDFConverter<EmailIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(EmailPerson.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<EmailIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<EmailIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
