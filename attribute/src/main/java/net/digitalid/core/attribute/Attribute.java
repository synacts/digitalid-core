package net.digitalid.core.attribute;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.Database;
import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.converter.sql.SQL;

import net.digitalid.core.conversion.wrappers.value.BooleanWrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.expression.PassiveExpression;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.property.nullable.NullableConceptProperty;
import net.digitalid.core.property.nullable.NullableConceptPropertyTable;
import net.digitalid.core.synchronizer.Synchronizer;

import net.digitalid.service.core.concept.GeneralConcept;

/**
 * This class models an attribute with its value and visibility.
 * 
 * @see AttributeModule
 */
@Immutable
public final class Attribute extends GeneralConcept implements SQL<Attribute> {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code published.attribute@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PUBLISHED = SemanticType.map("published.attribute@core.digitalid.net").load(BooleanWrapper.XDF_TYPE);
    
    /* -------------------------------------------------- Semantic Type -------------------------------------------------- */
    
    /**
     * Stores the semantic type of this attribute.
     * 
     * @invariant type.isAttributeFor(getEntity()) : "The type is an attribute for the entity.";
     */
    private final @Nonnull SemanticType type;
    
    /* -------------------------------------------------- Published Value -------------------------------------------------- */
    
    private static final @Nonnull NullableConceptPropertyTable<AttributeValue> table = NullableConceptPropertyTable.get("attribute_value", Attribute.FACTORY);
    
    public final @Nonnull NullableConceptProperty<AttributeValue, Attribute> value;
    
    /**
     * Stores the value of this attribute.
     * 
     * @invariant value == null || value.isVerified() && value.matches(this) : "The value is null or verified and matches this attribute.";
     */
    private @Nullable AttributeValue value;
    
    /* -------------------------------------------------- Unpublished Value -------------------------------------------------- */
    
    /**
     * Stores whether the unpublished value has already been loaded from the database.
     */
    private boolean unpublishedLoaded = false;
    
    /**
     * Stores the unpublished value of this attribute.
     * 
     * @invariant unpublished == null || unpublished.isVerified() && unpublished.matches(this) : "The unpublished value is null or verified and matches this attribute.";
     */
    private @Nullable AttributeValue unpublished;
    
    /* -------------------------------------------------- Visibility Expression -------------------------------------------------- */
    
    /**
     * Stores whether the visibility has already been loaded from the database.
     */
    private boolean visibilityLoaded = false;
    
    /**
     * Stores the visibility of this attribute.
     * 
     * @invariant visibility == null || visibility.getEntity().equals(getEntity()) : "The visibility is null or belongs to the same entity.";
     */
    private @Nullable PassiveExpression visibility;
    
    
    /**
     * Creates a new attribute with the given parameters.
     * 
     * @param entity the entity to which this attribute belongs.
     * @param type the type of this attribute.
     * 
     * @require type.isAttributeFor(entity) : "The type is an attribute for the entity.";
     */
    private Attribute(@Nonnull Entity entity, @Nonnull SemanticType type) {
        super(entity);
        
        Require.that(type.isAttributeFor(entity)).orThrow("The type is an attribute for the entity.");
        
        this.type = type;
    }
    
    
    /**
     * Returns the type of this attribute.
     * 
     * @return the type of this attribute.
     * 
     * @ensure return.isAttributeFor(getEntity()) : "The type is an attribute for the entity.";
     */
    public @Nonnull SemanticType getType() {
        return type;
    }
    
    
    /**
     * Returns the published value of this attribute or null if not set.
     * 
     * @return the published value of this attribute or null if not set.
     * 
     * @ensure return == null || return.isVerified() && return.matches(this) : "The returned value is null or verified matches this attribute.";
     */
    @Pure
    @NonCommitting
    public @Nullable AttributeValue getValue() throws DatabaseException {
        if (!valueLoaded) {
            value = AttributeModule.getValue(this, true);
            valueLoaded = true;
        }
        return value;
    }
    
    /**
     * Sets the published value of this attribute.
     * 
     * @param newValue the new value of this attribute.
     * 
     * @require isOnClient() : "This attribute is on a client.";
     * @require newValue == null || newValue.isVerified() && newValue.matches(this) : "The new value is null or verified and matches this attribute.";
     */
    @Committing
    public void setValue(@Nullable AttributeValue newValue) throws DatabaseException {
        final @Nullable AttributeValue oldValue = getValue();
        if (!Objects.equals(oldValue, newValue)) {
            Synchronizer.execute(new AttributeValueReplace(this, true, oldValue, newValue));
        }
    }
    
    /**
     * Replaces the published value of this attribute.
     * 
     * @param oldValue the old value of this attribute.
     * @param newValue the new value of this attribute.
     * 
     * @require !Objects.equals(oldValue, newValue) : "The old and new value are not equal.";
     * @require oldValue == null || oldValue.isVerified() && oldValue.matches(this) : "The old value is null or verified and matches this attribute.";
     * @require newValue == null || newValue.isVerified() && newValue.matches(this) : "The new value is null or verified and matches this attribute.";
     */
    @NonCommitting
    @OnlyForActions
    public void replaceValue(@Nullable AttributeValue oldValue, @Nullable AttributeValue newValue) throws DatabaseException {
        Require.that(!Objects.equals(oldValue, newValue)).orThrow("The old and new value are not equal.");
        
        if (oldValue == null && newValue != null) { AttributeModule.insertValue(this, true, newValue); }
        else if (oldValue != null && newValue == null) { AttributeModule.deleteValue(this, true, oldValue); }
        else if (oldValue != null && newValue != null) { AttributeModule.replaceValue(this, true, oldValue, newValue); }
        
        value = newValue;
        valueLoaded = true;
        notify(VALUE);
    }
    
    
    /**
     * Returns the unpublished value of this attribute or null if not set.
     * 
     * @return the unpublished value of this attribute or null if not set.
     * 
     * @ensure return == null || return.isVerified() && return.matches(this) : "The returned value is null or verified and matches this attribute.";
     */
    @Pure
    @NonCommitting
    public @Nullable AttributeValue getUnpublishedValue() throws DatabaseException {
        if (!unpublishedLoaded) {
            unpublished = AttributeModule.getValue(this, false);
            unpublishedLoaded = true;
        }
        return unpublished;
    }
    
    /**
     * Sets the unpublished value of this attribute.
     * 
     * @param newValue the new unpublished value of this attribute.
     * 
     * @require isOnClient() : "This attribute is on a client.";
     * @require newValue == null || newValue.isVerified() && newValue.matches(this) : "The new value is null or verified and matches this attribute.";
     */
    @Committing
    public void setUnpublishedValue(@Nullable AttributeValue newValue) throws DatabaseException {
        final @Nullable AttributeValue oldValue = getUnpublishedValue();
        if (!Objects.equals(oldValue, newValue)) {
            Synchronizer.execute(new AttributeValueReplace(this, false, oldValue, newValue));
        }
    }
    
    /**
     * Replaces the unpublished value of this attribute.
     * 
     * @param oldValue the old unpublished value of this attribute.
     * @param newValue the new unpublished value of this attribute.
     * 
     * @require !Objects.equals(oldValue, newValue) : "The old and new value are not equal.";
     * @require oldValue == null || oldValue.isVerified() && oldValue.matches(this) : "The old value is null or verified and matches this attribute.";
     * @require newValue == null || newValue.isVerified() && newValue.matches(this) : "The new value is null or verified and matches this attribute.";
     */
    @NonCommitting
    @OnlyForActions
    void replaceUnpublishedValue(@Nullable AttributeValue oldValue, @Nullable AttributeValue newValue) throws DatabaseException {
        Require.that(!Objects.equals(oldValue, newValue)).orThrow("The old and new value are not equal.");
        
        if (oldValue == null && newValue != null) { AttributeModule.insertValue(this, false, newValue); }
        else if (oldValue != null && newValue == null) { AttributeModule.deleteValue(this, false, oldValue); }
        else if (oldValue != null && newValue != null) { AttributeModule.replaceValue(this, false, oldValue, newValue); }
        
        unpublished = newValue;
        unpublishedLoaded = true;
        notify(UNPUBLISHED);
    }
    
    
    /**
     * Returns the visibility of this attribute or null if not set.
     * 
     * @return the visibility of this attribute or null if not set.
     * 
     * @require getEntity().getIdentity() instanceof InternalPerson : "The entity of this attribute belongs to an internal person.";
     * 
     * @ensure return == null || return.getEntity().equals(getEntity()) : "The returned visibility is null or belongs to the same entity.";
     */
    @Pure
    @NonCommitting
    public @Nullable PassiveExpression getVisibility() throws DatabaseException {
        if (!visibilityLoaded) {
            visibility = AttributeModule.getVisibility(this);
            visibilityLoaded = true;
        }
        return visibility;
    }
    
    /**
     * Sets the visibility of this attribute.
     * 
     * @param newVisibility the new visibility of this attribute.
     * 
     * @require isOnClient() : "This attribute is on a client.";
     * @require getEntity().getIdentity() instanceof InternalPerson : "The entity of this attribute belongs to an internal person.";
     * @require newVisibility == null || newVisibility.getEntity().equals(getEntity()) : "The new visibility is null or belongs to the same entity.";
     */
    @Committing
    public void setVisibility(@Nullable PassiveExpression newVisibility) throws DatabaseException {
        final @Nullable PassiveExpression oldVisibility = getVisibility();
        if (!Objects.equals(oldVisibility, newVisibility)) {
            Synchronizer.execute(new AttributeVisibilityReplace(this, oldVisibility, newVisibility));
        }
    }
    
    /**
     * Replaces the visibility of this attribute.
     * 
     * @param oldVisibility the old visibility of this attribute.
     * @param newVisibility the new visibility of this attribute.
     * 
     * @require !Objects.equals(oldVisibility, newVisibility) : "The old and new visibility are not equal.";
     * @require getEntity().getIdentity() instanceof InternalPerson : "The entity of this attribute belongs to an internal person.";
     * @require oldVisibility == null || oldVisibility.getEntity().equals(getEntity()) : "The old visibility is null or belongs to the same entity.";
     * @require newVisibility == null || newVisibility.getEntity().equals(getEntity()) : "The new visibility is null or belongs to the same entity.";
     */
    @NonCommitting
    @OnlyForActions
    void replaceVisibility(@Nullable PassiveExpression oldVisibility, @Nullable PassiveExpression newVisibility) throws DatabaseException {
        Require.that(!Objects.equals(oldVisibility, newVisibility)).orThrow("The old and new visibility are not equal.");
        
        if (oldVisibility == null && newVisibility != null) { AttributeModule.insertVisibility(this, newVisibility); }
        else if (oldVisibility != null && newVisibility == null) { AttributeModule.deleteVisibility(this, oldVisibility); }
        else if (oldVisibility != null && newVisibility != null) { AttributeModule.replaceVisibility(this, oldVisibility, newVisibility); }
        
        visibility = newVisibility;
        visibilityLoaded = true;
        notify(VISIBILITY);
    }
    
    
    /**
     * Resets this attribute.
     */
    public void reset() {
        this.valueLoaded = false;
        this.value = null;
        this.unpublishedLoaded = false;
        this.unpublished = null;
        this.visibilityLoaded = false;
        this.visibility = null;
        notify(RESET);
    }
    
    
    /**
     * Caches attributes given their entity and number.
     */
    private static final @Nonnull ConcurrentMap<Entity, ConcurrentMap<SemanticType, Attribute>> index = new ConcurrentHashMap<>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Entity.DELETED);
        }
    }
    
    /**
     * Returns a (locally cached) attribute that might not (yet) exist in the database.
     * 
     * @param entity the entity to which the attribute belongs.
     * @param type the type that denotes the attribute.
     * 
     * @return a new or existing attribute with the given entity and type.
     * 
     * @require type.isAttributeFor(entity) : "The type is an attribute for the entity.";
     */
    @Pure
    public static @Nonnull Attribute get(@Nonnull Entity entity, @Nonnull SemanticType type) {
        // TODO: Make the checkIsAttributeFor(entity) here? No, rather during decoding in the XDF converter!
        // Attribute.get(entity, IdentifierClass.create(tuple.getNonNullableElement(0)).getIdentity().castTo(SemanticType.class).checkIsAttributeFor(entity))
        
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<SemanticType, Attribute> map = index.get(entity);
            if (map == null) { map = index.putIfAbsentElseReturnPresent(entity, new ConcurrentHashMap<SemanticType, Attribute>()); }
            @Nullable Attribute context = map.get(type);
            if (context == null) { context = map.putIfAbsentElseReturnPresent(type, new Attribute(entity, type)); }
            return context;
        } else {
            return new Attribute(entity, type);
        }
    }
    
    /**
     * Returns all the attributes of the given entity.
     * 
     * @param entity the entity whose attributes are to be returned.
     * 
     * @return all the attributes of the given entity.
     * 
     * @ensure return.!isFrozen() : "The returned attributes are not frozen.";
     */
    @NonCommitting
    public static @Capturable @Nonnull FreezableSet<Attribute> getAll(@Nonnull Entity entity) throws DatabaseException {
        return AttributeModule.getAll(entity);
    }
    
    /**
     * Resets the attributes of the given entity after having reloaded the attributes module.
     * 
     * @param entity the entity whose attributes are to be reset.
     */
    public static void reset(@Nonnull NonHostEntity entity) {
        if (Database.isSingleAccess()) {
            final @Nullable ConcurrentMap<SemanticType, Attribute> map = index.get(entity);
            if (map != null) {
                for (final @Nonnull Attribute attribute : map.values()) { attribute.reset(); }
            }
        }
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean equals(Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof Attribute)) { return false; }
        final @Nonnull Attribute other = (Attribute) object;
        return this.getEntity().equals(other.getEntity()) && this.type.equals(other.type);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 41 * getEntity().hashCode() + type.hashCode();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Attribute (" + type.getAddress().getString() + ")";
    }
    
}
