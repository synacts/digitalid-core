package net.digitalid.core.typeset.authentications;

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

import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.typeset.FreezableAttributeTypeSet;

/**
 * This class models the authentications of nodes as a set of attribute types.
 */
@GenerateSubclass
@Freezable(ReadOnlyAuthentications.class)
public abstract class FreezableAuthentications extends FreezableAttributeTypeSet implements ReadOnlyAuthentications {
    
    /* -------------------------------------------------- Constants -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code identity.based.authentication.contact@core.digitalid.net}.
     */
    public static final @Nonnull @Loaded SemanticType IDENTITY_BASED_TYPE = SemanticType.map("identity.based.authentication.contact@core.digitalid.net").load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).withCategories(Category.INTERNAL_IDENTITIES).withCachingPeriod(Time.TROPICAL_YEAR).build());
    
    /**
     * Stores an empty set of authentications.
     */
    public static final @Nonnull ReadOnlyAuthentications NONE = FreezableAuthentications.withNoTypes().freeze();
    
    /**
     * Stores an identity-based authentication.
     */
    public static final @Nonnull ReadOnlyAuthentications IDENTITY_BASED = FreezableAuthentications.withType(IDENTITY_BASED_TYPE).freeze();
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    protected FreezableAuthentications() {}
    
    /**
     * Returns new authentications with no attribute types.
     */
    @Pure
    public static @Nonnull @NonFrozen @Single FreezableAuthentications withNoTypes() {
        return new FreezableAuthenticationsSubclass();
    }
    
    /**
     * Returns new authentications with the given attribute type.
     */
    @Pure
    public static @Nonnull @NonFrozen @Single FreezableAuthentications withType(@Nonnull @AttributeType SemanticType type) {
        final @Nonnull FreezableAuthentications result = new FreezableAuthenticationsSubclass();
        result.add(type);
        return result;
    }
    
    /**
     * Returns new authentications with the attribute types of the given authentications.
     */
    @Pure
    public static @Nonnull @NonFrozen FreezableAuthentications withTypesOf(@Nonnull ReadOnlyAuthentications authentications) {
        final @Nonnull FreezableAuthentications result = new FreezableAuthenticationsSubclass();
        result.addAll(authentications);
        return result;
    }
    
    /* -------------------------------------------------- Freezable -------------------------------------------------- */
    
    @Impure
    @Override
    @NonFrozenRecipient
    public @Chainable @Nonnull @Frozen ReadOnlyAuthentications freeze() {
        super.freeze();
        return this;
    }
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAuthentications clone() {
        return FreezableAuthentications.withTypesOf(this);
    }
    
}
