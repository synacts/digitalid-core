package net.digitalid.core.identifier;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.IdentityNotFoundException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.Category;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.Person;

/**
 * This class models email identifiers.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
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
    public @Nonnull Person getIdentity() throws SQLException, IOException, PacketException, ExternalException {
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
