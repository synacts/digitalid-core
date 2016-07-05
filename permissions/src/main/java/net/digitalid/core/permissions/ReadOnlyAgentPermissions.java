package net.digitalid.core.permissions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCapturable;
import net.digitalid.utility.collections.map.ReadOnlyMap;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.validation.annotations.type.ReadOnly;

import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.annotations.type.kind.AttributeType;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This interface provides read-only access to {@link FreezableAgentPermissions agent permissions} and should <em>never</em> be cast away.
 */
// TODO: @GenerateConverter // TODO: Do we need an @Recover method that generates the appropriate FreezableAgentPermissions here?
@ReadOnly(FreezableAgentPermissions.class)
public interface ReadOnlyAgentPermissions extends ReadOnlyMap<@Nonnull SemanticType, @Nonnull Boolean> {
    
    /* -------------------------------------------------- General Permission -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code general.permission.agent@core.digitalid.net}.
     */
    public static final @Nonnull @AttributeType SemanticType GENERAL = null; // TODO: SemanticType.map("general.permission.agent@core.digitalid.net").load(new Category[] {Category.HOST, Category.SYNTACTIC_TYPE, Category.SEMANTIC_TYPE, Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, BooleanWrapper.XDF_TYPE);
    
    /* -------------------------------------------------- Constants -------------------------------------------------- */
    
    /**
     * Stores an empty set of agent permissions.
     */
    public static final @Nonnull @Frozen ReadOnlyAgentPermissions NONE = FreezableAgentPermissions.withNoPermissions().freeze();
    
    /**
     * Stores a general read permission.
     */
    public static final @Nonnull @Frozen ReadOnlyAgentPermissions GENERAL_READ = FreezableAgentPermissions.with(GENERAL, false).freeze();
    
    /**
     * Stores a general write permission.
     */
    public static final @Nonnull @Frozen ReadOnlyAgentPermissions GENERAL_WRITE = FreezableAgentPermissions.with(GENERAL, true).freeze();
    
    /* -------------------------------------------------- Reading -------------------------------------------------- */
    
    /**
     * Returns whether these agent permissions allow to read the given type.
     */
    @Pure
    public default boolean allowToRead(@Nonnull @AttributeType SemanticType type) {
        return containsKey(type) || containsKey(GENERAL);
    }
    
    /**
     * Checks that these agent permissions allow to read the given type and throws a {@link RequestException} if not.
     */
    @Pure
    public default void checkAllowToRead(@Nonnull @AttributeType SemanticType type) throws RequestException {
        if (!allowToRead(type)) { throw RequestException.with(RequestErrorCode.AUTHORIZATION, "These agent permissions allow not to read $.", type.getAddress()); }
    }
    
    /* -------------------------------------------------- Writing -------------------------------------------------- */
    
    /**
     * Returns whether these permissions allow to write the given type.
     */
    @Pure
    public default boolean allowToWrite(@Nonnull @AttributeType SemanticType type)  {
        return containsKey(type) && get(type) || containsKey(GENERAL) && get(GENERAL);
    }
    
    /**
     * Checks that these agent permissions allow to write the given type and throws a {@link RequestException} if not.
     */
    @Pure
    public default void checkAllowToWrite(@Nonnull @AttributeType SemanticType type) throws RequestException {
        if (!allowToWrite(type)) { throw RequestException.with(RequestErrorCode.AUTHORIZATION, "These agent permissions allow not to write $.", type.getAddress()); }
    }
    
    /* -------------------------------------------------- Coverage -------------------------------------------------- */
    
    /**
     * Returns whether these agent permissions cover the given agent permissions.
     */
    @Pure
    public default boolean cover(@Nonnull ReadOnlyAgentPermissions permissions)  {
        final boolean generalPermission = containsKey(GENERAL);
        final boolean writingPermission = generalPermission ? get(GENERAL) : false;
        for (@Nonnull SemanticType type : permissions.keySet()) {
            if (containsKey(type)) {
                if (permissions.get(type) && !get(type)) { return false; }
            } else if (generalPermission) {
                if (permissions.get(type) && !writingPermission) { return false; }
            } else {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks that these agent permissions cover the given agent permissions and throws a {@link RequestException} if not.
     */
    @Pure
    public default void checkCover(@Nonnull ReadOnlyAgentPermissions permissions) throws RequestException {
        if (!cover(permissions)) { throw RequestException.with(RequestErrorCode.AUTHORIZATION, "These agent permissions do not cover $.", permissions); }
    }
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Returns the readable types of these permissions (excluding the also writable types).
     */
    @Pure
    public default @NonCapturable @Nonnull FiniteIterable<@Nonnull SemanticType> readableTypes() {
        return entrySet().filter(entry -> !entry.getValue()).map(entry -> entry.getKey());
    }
    
    /**
     * Returns the writable types of these permissions.
     */
    @Pure
    public default @NonCapturable @Nonnull FiniteIterable<@Nonnull SemanticType> writableTypes() {
        return entrySet().filter(entry -> entry.getValue()).map(entry -> entry.getKey());
    }
    
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
