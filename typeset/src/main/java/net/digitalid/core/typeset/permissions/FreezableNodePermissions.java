package net.digitalid.core.typeset.permissions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.freezable.annotations.Freezable;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.freezable.annotations.NonFrozenRecipient;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.method.Chainable;
import net.digitalid.utility.validation.annotations.size.Single;

import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.typeset.FreezableAttributeTypeSet;

/**
 * This class models the permissions of nodes as a set of attribute types.
 */
@GenerateSubclass
@Freezable(ReadOnlyNodePermissions.class)
public abstract class FreezableNodePermissions extends FreezableAttributeTypeSet implements ReadOnlyNodePermissions {
    
    /* -------------------------------------------------- Constants -------------------------------------------------- */
    
    /**
     * Stores an empty set of contact permissions.
     */
    public static final @Nonnull ReadOnlyNodePermissions NONE = FreezableNodePermissions.withNoTypes().freeze();
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    protected FreezableNodePermissions() {}
    
    /**
     * Returns new node permissions with no attribute types.
     */
    @Pure
    public static @Nonnull @NonFrozen @Single FreezableNodePermissions withNoTypes() {
        return new FreezableNodePermissionsSubclass();
    }
    
    /**
     * Returns new node permissions with the given attribute type.
     */
    @Pure
    public static @Nonnull @NonFrozen @Single FreezableNodePermissions withType(@Nonnull @AttributeType SemanticType type) {
        final @Nonnull FreezableNodePermissions result = new FreezableNodePermissionsSubclass();
        result.add(type);
        return result;
    }
    
    /**
     * Returns new node permissions with the attribute types of the given node permissions.
     */
    @Pure
    public static @Nonnull @NonFrozen FreezableNodePermissions withTypesOf(@Nonnull ReadOnlyNodePermissions permissions) {
        final @Nonnull FreezableNodePermissions result = new FreezableNodePermissionsSubclass();
        result.addAll(permissions);
        return result;
    }
    
    /* -------------------------------------------------- Freezable -------------------------------------------------- */
    
    @Impure
    @Override
    @NonFrozenRecipient
    public @Chainable @Nonnull @Frozen ReadOnlyNodePermissions freeze() {
        super.freeze();
        return this;
    }
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableNodePermissions clone() {
        return FreezableNodePermissions.withTypesOf(this);
    }
    
}
