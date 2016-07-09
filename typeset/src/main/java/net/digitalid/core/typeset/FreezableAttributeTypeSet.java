package net.digitalid.core.typeset;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.circumfixes.Brackets;
import net.digitalid.utility.collections.set.FreezableLinkedHashSet;
import net.digitalid.utility.freezable.annotations.Freezable;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.freezable.annotations.NonFrozenRecipient;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.method.Chainable;
import net.digitalid.utility.validation.annotations.size.Single;

import net.digitalid.core.identification.annotations.type.kind.AttributeType;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.typeset.authentications.FreezableAuthentications;
import net.digitalid.core.typeset.permissions.FreezableNodePermissions;

/**
 * This class models a freezable set of attribute types.
 * 
 * @see FreezableAuthentications
 * @see FreezableNodePermissions
 */
@GenerateSubclass
@Freezable(ReadOnlyAttributeTypeSet.class)
public abstract class FreezableAttributeTypeSet extends FreezableLinkedHashSet<@Nonnull @AttributeType SemanticType> implements ReadOnlyAttributeTypeSet {
    
    /* -------------------------------------------------- Modifications -------------------------------------------------- */
    
    @Impure
    @Override
    @NonFrozenRecipient
    public boolean add(@Nonnull @AttributeType SemanticType type) {
        return super.add(type);
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    protected FreezableAttributeTypeSet() {
        super(4, 0.75f);
    }
    
    /**
     * Returns a new attribute type set with no attribute types.
     */
    @Pure
    public static @Nonnull @NonFrozen @Single FreezableAttributeTypeSet withNoTypes() {
        return new FreezableAttributeTypeSetSubclass();
    }
    
    /**
     * Returns a new attribute type set with the given attribute type.
     */
    @Pure
    public static @Nonnull @NonFrozen @Single FreezableAttributeTypeSet withType(@Nonnull @AttributeType SemanticType type) {
        final @Nonnull FreezableAttributeTypeSet result = new FreezableAttributeTypeSetSubclass();
        result.add(type);
        return result;
    }
    
    /**
     * Returns a new attribute type set with the attribute types of the given set.
     */
    @Pure
    public static @Nonnull @NonFrozen FreezableAttributeTypeSet withTypesOf(@Nonnull ReadOnlyAttributeTypeSet set) {
        final @Nonnull FreezableAttributeTypeSet result = new FreezableAttributeTypeSetSubclass();
        result.addAll(set);
        return result;
    }
    
    /* -------------------------------------------------- Freezable -------------------------------------------------- */
    
    @Impure
    @Override
    @NonFrozenRecipient
    public @Chainable @Nonnull @Frozen ReadOnlyAttributeTypeSet freeze() {
        super.freeze();
        return this;
    }
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAttributeTypeSet clone() {
        return FreezableAttributeTypeSet.withTypesOf(this);
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return map(type -> type.getAddress()).join(Brackets.CURLY);
    }
    
    /* -------------------------------------------------- Exports -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull FreezableAgentPermissions toAgentPermissions() {
        final @Nonnull FreezableAgentPermissions permissions = FreezableAgentPermissions.withNoPermissions();
        for (@Nonnull SemanticType type : this) {
            permissions.put(type, false);
        }
        return permissions;
    }
    
}
