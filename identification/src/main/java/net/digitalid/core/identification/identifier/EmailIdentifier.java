package net.digitalid.core.identification.identifier;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Person;

/**
 * This class models email identifiers.
 * 
 * (This type has to be a class because otherwise the static {@link #isValid(java.lang.String)} method would not be inherited by the generated subclass.)
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class EmailIdentifier extends ExternalIdentifier {
    
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
    public @Nonnull Person resolve() throws ExternalException {
        return IdentifierResolver.configuration.get().resolve(this).castTo(Person.class);
    }
    
}
