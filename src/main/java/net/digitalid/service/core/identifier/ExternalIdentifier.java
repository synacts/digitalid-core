package net.digitalid.service.core.identifier;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.Category;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.Person;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;

/**
 * This class models external identifiers.
 * 
 * @see EmailIdentifier
 * @see MobileIdentifier
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
    public final @Nonnull Person getMappedIdentity() throws AbortException {
        assert isMapped() : "This identifier is mapped.";
        
        final @Nonnull Identity identity = Mapper.getMappedIdentity(this);
        if (identity instanceof Person) return (Person) identity;
        else throw new SQLException("The mapped identity has a wrong type.");
    }
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nonnull Person getIdentity() throws AbortException, PacketException, ExternalException, NetworkException;
    
    
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
