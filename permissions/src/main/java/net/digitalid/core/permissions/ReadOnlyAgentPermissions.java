package net.digitalid.core.permissions;


import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.map.ReadOnlyMap;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.validation.annotations.type.ReadOnly;

import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.annotations.type.AttributeType;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This interface provides read-only access to {@link FreezableAgentPermissions agent permissions} and should <em>never</em> be cast away.
 */
@ReadOnly(FreezableAgentPermissions.class)
public interface ReadOnlyAgentPermissions extends ReadOnlyMap<SemanticType, Boolean> {
    
    /* -------------------------------------------------- Reading -------------------------------------------------- */
    
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
    public boolean canRead(@Nonnull @AttributeType SemanticType type);
    
    /**
     * Checks that an agent with these agent permissions can read the given type and throws a {@link RequestException} if not.
     * 
     * @param type the semantic type to check.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public void checkCanRead(@Nonnull SemanticType type) throws RequestException;
    
    /* -------------------------------------------------- Writing -------------------------------------------------- */
    
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
     * Checks that an agent with these agent permissions can write the given type and throws a {@link RequestException} if not.
     * 
     * @param type the semantic type to check.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    @Pure
    public void checkCanWrite(@Nonnull SemanticType type) throws RequestException;
    
    /* -------------------------------------------------- Coverage -------------------------------------------------- */
    
    /**
     * Returns whether these agent permissions cover the given agent permissions.
     * 
     * @param permissions the agent permissions that need to be covered.
     * 
     * @return whether these agent permissions cover the given agent permissions.
     */
    @Pure
    public boolean cover(@Nonnull ReadOnlyAgentPermissions permissions);
    
    /**
     * Checks that these agent permissions cover the given agent permissions and throws a {@link RequestException} if not.
     * 
     * @param permissions the agent permissions that need to be covered.
     */
    @Pure
    public void checkCover(@Nonnull ReadOnlyAgentPermissions permissions) throws RequestException;
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAgentPermissions clone();
    
    /* -------------------------------------------------- Database -------------------------------------------------- */
    
    // TODO: Remove the following code if the database module can handle inline maps.
    
//    /**
//     * Sets the parameters at the given start index of the prepared statement to this object.
//     * 
//     * @param preparedStatement the prepared statement whose parameters are to be set.
//     * @param startIndex the start index of the parameters to set.
//     * 
//     * @require areEmptyOrSingle() : "These permissions are empty or single.";
//     */
//    @NonCommitting
//    public void setEmptyOrSingle(@Nonnull PreparedStatement preparedStatement, int startIndex) throws DatabaseException;
    
}
