package ch.virtualid.agent;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.PacketException;
import ch.virtualid.util.ReadonlyMap;
import ch.virtualid.interfaces.Blockable;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link Permissions permissions} and should <em>never</em> be cast away.
 * 
 * @invariant areValid() : "These permissions are always valid.";
 * 
 * @see Permissions
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyPermissions extends ReadonlyMap<SemanticType, Boolean>, Blockable {
    
    /**
     * Returns whether these permissions are valid.
     * 
     * @return whether these permissions are valid.
     */
    @Pure
    public boolean areValid();
    
    /**
     * Returns whether these permissions contain a single one.
     * 
     * @return whether these permissions contain a single one.
     */
    @Pure
    public boolean areSingle();
    
    /**
     * Returns whether an agent with these permissions can read the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether an agent with these permissions can read the given type.
     */
    @Pure
    public boolean canRead(@Nonnull SemanticType type);
    
    /**
     * Checks that an agent with these permissions can read the given type and throws a {@link PacketException} if not.
     * 
     * @param type the semantic type to check.
     */
    @Pure
    public void checkRead(@Nonnull SemanticType type) throws PacketException;
    
    /**
     * Returns whether an agent with these permissions can write the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether an agent with these permissions can write the given type.
     */
    @Pure
    public boolean canWrite(@Nonnull SemanticType type);
    
    /**
     * Checks that an agent with these permissions can write the given type and throws a {@link PacketException} if not.
     * 
     * @param type the semantic type to check.
     */
    @Pure
    public void checkWrite(@Nonnull SemanticType type) throws PacketException;
    
    /**
     * Returns whether these permissions cover the given permissions.
     * 
     * @param permissions the permissions that need to be covered.
     * 
     * @return whether these permissions cover the given permissions.
     */
    @Pure
    public boolean cover(@Nonnull ReadonlyPermissions permissions);
    
    /**
     * Checks that these permissions cover the given permissions and throws a {@link PacketException} if not.
     * 
     * @param permissions the permissions that need to be covered.
     */
    @Pure
    public void checkCoverage(@Nonnull ReadonlyPermissions permissions) throws PacketException;
    
}
