package ch.virtualid.identifier;

import ch.virtualid.annotations.Pure;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.ExternalIdentity;
import ch.virtualid.interfaces.Immutable;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class represents external identifiers.
 * 
 * @see EmailIdentifier
 * @see MobileIdentifier
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class ExternalIdentifier extends Identifier implements Immutable {
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     *
     * @param string the string to check.
     * 
     * @return whether the given string conforms to the criteria of this class.
     */
    @Pure
    static boolean isConforming(@Nonnull String string) {
        return Identifier.isConforming(string) && string.contains(":");
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
    public abstract @Nonnull ExternalIdentity getIdentity() throws SQLException, IOException, PacketException, ExternalException;
    
    
    /**
     * Returns the category of this external identifier.
     * 
     * @return the category of this external identifier.
     */
    @Pure
    public abstract @Nonnull Category getCategory();
    
}
