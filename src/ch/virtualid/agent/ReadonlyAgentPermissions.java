package ch.virtualid.agent;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.util.ReadonlyMap;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This interface provides readonly access to {@link AgentPermissions agent permissions} and should <em>never</em> be cast away.
 * 
 * @invariant areValid() : "These agent permissions are always valid.";
 * 
 * @see AgentPermissions
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyAgentPermissions extends ReadonlyMap<SemanticType, Boolean>, Blockable {
    
    /**
     * Returns whether these agent permissions are valid.
     * 
     * @return whether these agent permissions are valid.
     */
    @Pure
    public boolean areValid();
    
    /**
     * Returns whether these agent permissions contain a single permission.
     * 
     * @return whether these agent permissions contain a single permission.
     */
    @Pure
    public boolean areSingle();
    
    /**
     * Returns whether these agent permissions are empty or contain a single permission.
     * 
     * @return whether these agent permissions are empty or contain a single permission.
     */
    @Pure
    public boolean areEmptyOrSingle();
    
    /**
     * Returns whether an agent with these agent permissions can read the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether an agent with these agent permissions can read the given type.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public boolean canRead(@Nonnull SemanticType type);
    
    /**
     * Checks that an agent with these agent permissions can read the given type and throws a {@link PacketException} if not.
     * 
     * @param type the semantic type to check.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public void checkCanRead(@Nonnull SemanticType type) throws PacketException;
    
    /**
     * Returns whether an agent with agent these permissions can write the given type.
     * 
     * @param type the semantic type to check.
     * 
     * @return whether an agent with agent these permissions can write the given type.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public boolean canWrite(@Nonnull SemanticType type);
    
    /**
     * Checks that an agent with these agent permissions can write the given type and throws a {@link PacketException} if not.
     * 
     * @param type the semantic type to check.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public void checkCanWrite(@Nonnull SemanticType type) throws PacketException;
    
    /**
     * Returns whether these agent permissions cover the given agent permissions.
     * 
     * @param permissions the agent permissions that need to be covered.
     * 
     * @return whether these agent permissions cover the given agent permissions.
     */
    @Pure
    public boolean cover(@Nonnull ReadonlyAgentPermissions permissions);
    
    /**
     * Checks that these agent permissions cover the given agent permissions and throws a {@link PacketException} if not.
     * 
     * @param permissions the agent permissions that need to be covered.
     */
    @Pure
    public void checkCover(@Nonnull ReadonlyAgentPermissions permissions) throws PacketException;
    
    
    @Pure
    @Override
    public @Capturable @Nonnull AgentPermissions clone();
    
    
    /**
     * Returns all types of these permissions as numbers.
     * 
     * @return all types of these permissions as numbers.
     */
    @Pure
    public @Nonnull String allTypesToString();
    
    /**
     * Returns the write types of these permissions as numbers.
     * 
     * @return the write types of these permissions as numbers.
     */
    @Pure
    public @Nonnull String writeTypesToString();
    
    
    /**
     * Sets the parameters at the given start index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param startIndex the start index of the parameters to set.
     */
    public void set(@Nonnull PreparedStatement preparedStatement, int startIndex) throws SQLException;
    
    /**
     * Sets the parameters at the given start index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param startIndex the start index of the parameters to set.
     * 
     * @require areEmptyOrSingle() : "These permissions are empty or single.";
     */
    public void setEmptyOrSingle(@Nonnull PreparedStatement preparedStatement, int startIndex) throws SQLException;
    
}
