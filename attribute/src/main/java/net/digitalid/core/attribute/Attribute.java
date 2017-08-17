package net.digitalid.core.attribute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.set.FreezableHashSetBuilder;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.validation.annotations.generation.Provide;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.expression.PassiveExpression;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.property.RequiredAuthorization;
import net.digitalid.core.property.RequiredAuthorizationBuilder;
import net.digitalid.core.signature.attribute.AttributeValue;
import net.digitalid.core.signature.attribute.UncertifiedAttributeValue;
import net.digitalid.core.subject.CoreServiceCoreSubject;
import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;

/**
 * This class models an attribute with its value and visibility.
 * 
 * @invariant getKey().isAttributeFor(getEntity().getIdentity().getCategory()) : "The type is an attribute for the entity of this attribute.";
 */
@Immutable
@GenerateSubclass
@GenerateTableConverter(table = "unit_core_Attribute_Attribute") // TODO: How can we get the table name without adding the generated attribute table converter to the attribute core subject module?
public abstract class Attribute extends CoreServiceCoreSubject<Entity, SemanticType> {
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        super.validate();
        
        Validate.that(getKey().isAttributeFor(getEntity().getIdentity().getCategory())).orThrow("The type has to be an attribute for the entity of this attribute.");
    }
    
    /* -------------------------------------------------- Published Value -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the published value.
     */
    static final @Nonnull RequiredAuthorization<Entity, SemanticType, Attribute, @Nullable AttributeValue> VALUE = RequiredAuthorizationBuilder.<Entity, SemanticType, Attribute, AttributeValue>withRequiredPermissionsToExecuteMethod((concept, value) -> FreezableAgentPermissions.withPermission(concept.getKey(), true)).withRequiredPermissionsToSeeMethod((concept, value) -> FreezableAgentPermissions.withPermission(concept.getKey(), false)).build();
    
    /**
     * Returns the published value property of this attribute.
     * 
     * TODO: @invariant value == null || value.isVerified() && value.matches(this) : "The value is null or verified and matches this attribute.";
     */
    @Pure
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<Attribute, @Nullable AttributeValue> value();
    
    /* -------------------------------------------------- Unpublished Value -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the unpublished value.
     */
    static final @Nonnull RequiredAuthorization<Entity, SemanticType, Attribute, @Nullable UncertifiedAttributeValue> UNPUBLISHED = RequiredAuthorizationBuilder.<Entity, SemanticType, Attribute, UncertifiedAttributeValue>withRequiredPermissionsToExecuteMethod((concept, value) -> FreezableAgentPermissions.withPermission(concept.getKey(), true)).withRequiredPermissionsToSeeMethod((concept, value) -> FreezableAgentPermissions.withPermission(concept.getKey(), false)).build();
    
    /**
     * Returns the unpublished value property of this attribute.
     * 
     * TODO: @invariant unpublished == null || unpublished.isVerified() && unpublished.matches(this) : "The unpublished value is null or verified and matches this attribute.";
     */
    @Pure
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<Attribute, @Nullable UncertifiedAttributeValue> unpublished();
    
    /* -------------------------------------------------- Visibility Expression -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the visibility.
     */
    static final @Nonnull RequiredAuthorization<Entity, SemanticType, Attribute, @Nullable PassiveExpression> VISIBILITY = RequiredAuthorizationBuilder.<Entity, SemanticType, Attribute, PassiveExpression>withRequiredPermissionsToExecuteMethod((concept, value) -> FreezableAgentPermissions.withPermission(concept.getKey(), true)).withRequiredPermissionsToSeeMethod((concept, value) -> FreezableAgentPermissions.withPermission(concept.getKey(), false)).build();
    
    /**
     * Returns the visibility property of this attribute.
     */
    @Pure
    @GenerateSynchronizedProperty
    @Provide("attribute -> (NonHostEntity) attribute.getEntity()")
    public abstract @Nonnull WritablePersistentValueProperty<Attribute, @Nullable PassiveExpression> visibility();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the potentially cached attribute of the given entity with the given type after having inserted it into its database table.
     */
    @Pure
    @Recover
    public static @Nonnull Attribute of(@Nonnull Entity entity, @Nonnull SemanticType key) throws DatabaseException {
        return AttributeSubclass.MODULE.getSubjectIndex().get(entity, key);
    }
    
    /**
     * Returns all the attributes of the given entity.
     */
    @Pure
    @NonCommitting
    @TODO(task = "Do we need/want such a method?", date = "2016-12-02", author = Author.KASPAR_ETTER)
    public static @Capturable @Nonnull @NonFrozen FreezableSet<Attribute> getAll(@Nonnull Entity entity) throws DatabaseException {
        return FreezableHashSetBuilder.build();
//        return AttributeModule.getAll(entity);
    }
    
}
