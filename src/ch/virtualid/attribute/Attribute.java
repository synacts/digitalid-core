package ch.virtualid.attribute;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Committing;
import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.GeneralConcept;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.expression.PassiveExpression;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.synchronizer.Synchronizer;
import ch.virtualid.collections.ConcurrentHashMap;
import ch.virtualid.collections.ConcurrentMap;
import ch.virtualid.collections.FreezableSet;
import ch.xdf.BooleanWrapper;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an attribute with its value and visibility.
 * 
 * @see AttributeModule
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Attribute extends GeneralConcept implements Immutable {
    
    /**
     * Stores the aspect of a published attribute changing its value.
     */
    public static final @Nonnull Aspect VALUE = new Aspect(Attribute.class, "value");
    
    /**
     * Stores the aspect of an unpublished attribute changing its value.
     */
    public static final @Nonnull Aspect UNPUBLISHED = new Aspect(Attribute.class, "unpublished");
    
    /**
     * Stores the aspect of an attribute changing its visibility.
     */
    public static final @Nonnull Aspect VISIBILITY = new Aspect(Attribute.class, "visibility");
    
    /**
     * Stores the aspect of an attribute being reset after having reloaded the attributes module.
     */
    public static final @Nonnull Aspect RESET = new Aspect(Attribute.class, "attribute reset");
    
    
    /**
     * Stores the semantic type {@code published.value.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType PUBLISHED = SemanticType.create("published.value.attribute@virtualid.ch").load(BooleanWrapper.TYPE);
    
    
    /**
     * Stores the type of this attribute.
     * 
     * @invariant type.isAttributeFor(getEntity()) : "The type is an attribute for the entity.";
     */
    private final @Nonnull SemanticType type;
    
    
    /**
     * Stores whether the value has already been loaded from the database.
     */
    private boolean valueLoaded = false;
    
    /**
     * Stores the value of this attribute.
     * 
     * @invariant value == null || value.isVerified() && value.matches(this) : "The value is null or verified and matches this attribute.";
     */
    private @Nullable AttributeValue value;
    
    
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
        
        assert type.isAttributeFor(entity) : "The type is an attribute for the entity.";
        
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
    public @Nullable AttributeValue getValue() throws SQLException {
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
    public void setValue(@Nullable AttributeValue newValue) throws SQLException {
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
    public void replaceValue(@Nullable AttributeValue oldValue, @Nullable AttributeValue newValue) throws SQLException {
        assert !Objects.equals(oldValue, newValue) : "The old and new value are not equal.";
        
        if (oldValue == null && newValue != null) AttributeModule.insertValue(this, true, newValue);
        else if (oldValue != null && newValue == null) AttributeModule.deleteValue(this, true, oldValue);
        else if (oldValue != null && newValue != null) AttributeModule.replaceValue(this, true, oldValue, newValue);
        
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
    public @Nullable AttributeValue getUnpublishedValue() throws SQLException {
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
    public void setUnpublishedValue(@Nullable AttributeValue newValue) throws SQLException {
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
    void replaceUnpublishedValue(@Nullable AttributeValue oldValue, @Nullable AttributeValue newValue) throws SQLException {
        assert !Objects.equals(oldValue, newValue) : "The old and new value are not equal.";
        
        if (oldValue == null && newValue != null) AttributeModule.insertValue(this, false, newValue);
        else if (oldValue != null && newValue == null) AttributeModule.deleteValue(this, false, oldValue);
        else if (oldValue != null && newValue != null) AttributeModule.replaceValue(this, false, oldValue, newValue);
        
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
    public @Nullable PassiveExpression getVisibility() throws SQLException {
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
    public void setVisibility(@Nullable PassiveExpression newVisibility) throws SQLException {
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
    void replaceVisibility(@Nullable PassiveExpression oldVisibility, @Nullable PassiveExpression newVisibility) throws SQLException {
        assert !Objects.equals(oldVisibility, newVisibility) : "The old and new visibility are not equal.";
        
        if (oldVisibility == null && newVisibility != null) AttributeModule.insertVisibility(this, newVisibility);
        else if (oldVisibility != null && newVisibility == null) AttributeModule.deleteVisibility(this, oldVisibility);
        else if (oldVisibility != null && newVisibility != null) AttributeModule.replaceVisibility(this, oldVisibility, newVisibility);
        
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
    private static final @Nonnull ConcurrentMap<Entity, ConcurrentMap<SemanticType, Attribute>> index = new ConcurrentHashMap<Entity, ConcurrentMap<SemanticType, Attribute>>();
    
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
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<SemanticType, Attribute> map = index.get(entity);
            if (map == null) map = index.putIfAbsentElseReturnPresent(entity, new ConcurrentHashMap<SemanticType, Attribute>());
            @Nullable Attribute context = map.get(type);
            if (context == null) context = map.putIfAbsentElseReturnPresent(type, new Attribute(entity, type));
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
     * @ensure return.isNotFrozen() : "The returned attributes are not frozen.";
     */
    @NonCommitting
    public static @Capturable @Nonnull FreezableSet<Attribute> getAll(@Nonnull Entity entity) throws SQLException {
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
            if (map != null) for (final @Nonnull Attribute attribute : map.values()) attribute.reset();
        }
    }
    
    
    @Pure
    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Attribute)) return false;
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
