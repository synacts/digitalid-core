package net.digitalid.core.identification.identifier;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.exceptions.IdentityNotFoundException;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Person;

/**
 * This interface models email identifiers.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public interface EmailIdentifier extends ExternalIdentifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * The pattern that valid email identifiers have to match.
     */
    public static final @Nonnull Pattern PATTERN = Pattern.compile("email:[a-z0-9]+(?:[._-][a-z0-9]+)*@[a-z0-9]+(?:[.-][a-z0-9]+)*\\.[a-z][a-z]+");
    
    /**
     * Returns whether the given string is a valid email identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return ExternalIdentifier.isConforming(string) && PATTERN.matcher(string).matches();
    }
    
    /* -------------------------------------------------- Existence -------------------------------------------------- */
    
    /**
     * Returns the host of this email address.
     */
    @Pure
    public default @Nonnull String getHost() {
        return getString().substring(getString().indexOf('@') + 1);
    }
    
    /**
     * Returns whether the provider of this email address exists.
     */
    @Pure
    public default boolean providerExists() {
        // TODO: These classes do not seem to exist on Android.
        
//import javax.naming.NamingException;
//import javax.naming.directory.Attributes;
//import javax.naming.directory.InitialDirContext;
//        try {
//            final @Nonnull InitialDirContext context = new InitialDirContext();
//            final @Nonnull Attributes attributes = context.getAttributes("dns:/" + getHost(), new String[] {"MX"});
//            return attributes.get("MX") != null;
//        } catch (@Nonnull NamingException exception) {
//            return false;
//        }
        
        return false;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns an email identifier with the given string.
     */
    @Pure
    @Recover
    public static @Nonnull EmailIdentifier with(@Nonnull @Valid String string) {
        return new EmailIdentifierSubclass(string);
    }
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public default @Nonnull Person resolve() throws ExternalException {
        if (!providerExists()) { throw IdentityNotFoundException.with(this); }
        return IdentifierResolver.resolve(this).castTo(Person.class);
    }
    
}
