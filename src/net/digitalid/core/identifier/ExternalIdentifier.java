package net.digitalid.core.identifier;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.errors.ShouldNeverHappenError;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.Category;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.Person;

/**
 * This class models external identifiers.
 * 
 * @see EmailIdentifier
 * @see MobileIdentifier
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class ExternalIdentifier extends IdentifierClass implements NonHostIdentifier {
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     *
     * @param string the string to check.
     * 
     * @return whether the given string conforms to the criteria of this class.
     */
    @Pure
    static boolean isConforming(@Nonnull String string) {
        return IdentifierClass.isConforming(string) && string.contains(":");
    }
    
    /**
     * Returns whether the given string is a valid identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        final int index = string.indexOf(":");
        if (index < 1) return false;
        final @Nonnull String scheme = string.substring(0, index);
        switch (scheme) {
            case "email": return EmailIdentifier.isValid(string);
            case "mobile": return MobileIdentifier.isValid(string);
            default: return false;
        }
    }
    
    /**
     * Returns a new external identifier with the given string.
     * 
     * @param string the string of the new external identifier.
     * 
     * @return a new external identifier with the given string.
     * 
     * @require isValid(string) : "The string is a valid external identifier.";
     */
    @Pure
    public static @Nonnull Identifier create(@Nonnull String string) {
        assert isValid(string) : "The string is a valid external identifier.";
        
        final @Nonnull String scheme = string.substring(0, string.indexOf(":"));
        switch (scheme) {
            case "email": return new EmailIdentifier(string);
            case "mobile": return new MobileIdentifier(string);
            default: throw new ShouldNeverHappenError("The scheme '" + scheme + "' is not valid.");
        }
    }
    
    
    /**
     * Creates an external identifier with the given string.
     * 
     * @param string the string of the external identifier.
     * 
     * @require isValid(string) : "The string is a valid external identifier.";
     */
    ExternalIdentifier(@Nonnull String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid external identifier.";
    }
    
    
    @Pure
    @Override
    @NonCommitting
    public final @Nonnull Person getMappedIdentity() throws SQLException {
        assert isMapped() : "This identifier is mapped.";
        
        final @Nonnull Identity identity = Mapper.getMappedIdentity(this);
        if (identity instanceof Person) return (Person) identity;
        else throw new SQLException("The mapped identity has a wrong type.");
    }
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nonnull Person getIdentity() throws SQLException, IOException, PacketException, ExternalException;
    
    
    /**
     * Returns the category of this external identifier.
     * 
     * @return the category of this external identifier.
     * 
     * @ensure return.isExternalPerson() : "The returned category denotes an external person.";
     */
    @Pure
    public abstract @Nonnull Category getCategory();
    
}
