package net.digitalid.core.identifier;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.Category;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.Person;

/**
 * This class models mobile identifiers.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
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
    public @Nonnull Person getIdentity() throws SQLException, IOException, PacketException, ExternalException {
        return Mapper.getIdentity(this).toPerson();
    }
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.MOBILE_PERSON;
    }
    
}
