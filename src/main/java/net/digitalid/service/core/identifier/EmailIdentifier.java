package net.digitalid.service.core.identifier;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.IdentityNotFoundException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.Category;
import net.digitalid.service.core.identity.Mapper;
import net.digitalid.service.core.identity.Person;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models email identifiers.
 */
@Immutable
public final class EmailIdentifier extends ExternalIdentifier {
    
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
    
    
    /**
     * Creates an email identifier with the given string.
     * 
     * @param string the string of the email identifier.
     * 
     * @require isValid(string) : "The string is a valid email identifier.";
     */
    public EmailIdentifier(@Nonnull String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid email identifier.";
    }
    
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Person getIdentity() throws AbortException, PacketException, ExternalException, NetworkException {
        if (!providerExists()) throw new IdentityNotFoundException(this);
        return Mapper.getIdentity(this).toPerson();
    }
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.EMAIL_PERSON;
    }
    
    
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
    
}
