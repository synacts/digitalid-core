package ch.virtualid.identifier;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.MobilePerson;
import ch.virtualid.interfaces.Immutable;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * This class represents mobile identifiers.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class MobileIdentifier extends ExternalIdentifier implements Immutable {
    
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
    
    
    /**
     * Creates a mobile identifier with the given string.
     * 
     * @param string the string of the mobile identifier.
     * 
     * @require isValid(string) : "The string is a valid mobile identifier.";
     */
    public MobileIdentifier(@Nonnull String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid mobile identifier.";
    }
    
    
    @Pure
    @Override
    public @Nonnull MobilePerson getIdentity() throws SQLException, InvalidEncodingException {
        return Mapper.mapExternalIdentity(this).toMobilePerson();
    }
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.MOBILE_PERSON;
    }
    
}
