package net.digitalid.service.core.identifier;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.Category;
import net.digitalid.service.core.identity.Mapper;
import net.digitalid.service.core.identity.Person;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models mobile identifiers.
 */
@Immutable
public final class MobileIdentifier extends ExternalIdentifier {
    
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
    @NonCommitting
    public @Nonnull Person getIdentity() throws AbortException, PacketException, ExternalException, NetworkException {
        return Mapper.getIdentity(this).toPerson();
    }
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.MOBILE_PERSON;
    }
    
}
