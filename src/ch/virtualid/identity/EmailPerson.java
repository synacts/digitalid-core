package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

/**
 * This class models the email person virtual identities.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class EmailPerson extends ExternalPerson implements Immutable {
    
    /**
     * Stores the semantic type {@code email.person@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("email.person@virtualid.ch").load(Person.IDENTIFIER);
    
    
    /**
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    EmailPerson(long number, @Nonnull NonHostIdentifier address) {
        super(number, address);
    }
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.EXTERNAL_PERSON;
    }
    
    
    /**
     * Returns whether the provider of the given email address exists.
     * 
     * @param identifier the email address of interest.
     * 
     * @return whether the provider of the given email address exists.
     */
    @Pure
    @Deprecated
    public static boolean providerExists(@Nonnull NonHostIdentifier identifier) {
        try {
            final @Nonnull InitialDirContext context = new InitialDirContext();
            final @Nonnull Attributes attributes = context.getAttributes("dns:/" + identifier.getHostIdentifier().getString(), new String[] {"MX"});
            return attributes.get("MX") != null;
        } catch (@Nonnull NamingException exception) {
            return false;
        }
    }
    
}
