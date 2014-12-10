package ch.virtualid.concepts;

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
import ch.virtualid.module.both.Attributes;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import ch.xdf.BooleanWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import java.sql.SQLException;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an attribute with its value and visibility.
 * 
 * @see Attributes
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
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
     * Stores the semantic type {@code published.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType PUBLISHED = SemanticType.create("published.attribute@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("attribute@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    
    /**
     * Stores the type of this attribute.
     * 
     * @invariant type.isAttributeType() : "The type is an attribute type.";
     */
    private final @Nonnull SemanticType type;
    
    
    /**
     * Stores whether the value has already been loaded from the database.
     */
    private boolean valueLoaded = false;
    
    /**
     * Stores the value of this attribute.
     */
    private @Nullable SignatureWrapper value;
    
    
    /**
     * Stores whether the unpublished value has already been loaded from the database.
     */
    private boolean unpublishedLoaded = false;
    
    /**
     * Stores the unpublished value of this attribute.
     */
    private @Nullable SignatureWrapper unpublished;
    
    
    /**
     * Stores whether the visibility has already been loaded from the database.
     */
    private boolean visibilityLoaded = false;
    
    /**
     * Stores the visibility of this attribute.
     */
    private @Nullable PassiveExpression visibility;
    
    
    /**
     * Creates a new attribute with the given parameters.
     * 
     * @param entity the entity to which this attribute belongs.
     * @param type the type of this attribute.
     * 
     * @require type.isAttributeType() : "The type is an attribute type.";
     */
    private Attribute(@Nonnull Entity entity, @Nonnull SemanticType type) {
        super(entity);
        
        assert type.isAttributeType() : "The type is an attribute type.";
        
        this.type = type;
    }
    
    
    /**
     * Returns the type of this attribute.
     * 
     * @return the type of this attribute.
     * 
     * @ensure return.isAttributeType() : "The type is an attribute type.";
     */
    public @Nonnull SemanticType getType() {
        return type;
    }
    
    
    /**
     * Returns the value of this attribute or null if not yet set.
     * 
     * @return the value of this attribute or null if not yet set.
     */
    public @Nullable SignatureWrapper getValue() throws SQLException {
        if (!valueLoaded) {
//            value = Attributes.getValue(this, true);
            valueLoaded = true;
        }
        return value;
    }
    
    public void setValue(@Nullable SignatureWrapper value) throws SQLException {
        assert isOnClient() : "";
        
        // The old and the new value is not identical (and particularly not both null).
        if (this.value != value) { // TODO: Rather a precondition? -> No, rather a real equals().
//            Synchronizer.execute(new AttributeValueReplace(this, true, getValue(), value));
        }
    }
    
    @OnlyForActions
    public void replaceValue(@Nullable SignatureWrapper oldValue, @Nullable SignatureWrapper newValue) throws SQLException {
        assert oldValue != newValue : "";
        
//        if (oldValue == null) Attributes.addValue(this, true, newValue);
//        else if (newValue == null) Attributes.removeValue(this, true, oldValue);
//        else Attributes.replaceValue(this, true, oldValue, newValue);
        
        value = newValue;
        valueLoaded = true;
        
        notify(VALUE);
    }
    
    
    /**
     * Returns the unpublished value of this attribute or null if not yet set or available.
     * 
     * @return the unpublished value of this attribute or null if not yet set or available.
     */
    public @Nullable SignatureWrapper getUnpublishedValue() {
        if (!unpublishedLoaded) {
//            unpublished = Attributes.getValue(this);
            unpublishedLoaded = true;
        }
        return unpublished;
    }
    
    
    /**
     * Returns the visibility of this attribute or null if not yet set or available.
     * 
     * @return the visibility of this attribute or null if not yet set or available.
     */
    public @Nullable PassiveExpression getVisibility() {
        if (!visibilityLoaded) {
//            visibility = Attributes.getValue(this);
            visibilityLoaded = true;
        }
        return visibility;
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
     */
    public static @Nonnull Set<Attribute> getAll(@Nonnull Entity entity) throws SQLException {
        throw new SQLException();
//        return Attributes.getAll(entity);
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
                for (final @Nonnull Attribute attribute : map.values()) {
                    attribute.valueLoaded = false;
                    attribute.value = null;
                    attribute.unpublishedLoaded = false;
                    attribute.unpublished = null;
                    attribute.visibilityLoaded = false;
                    attribute.visibility = null;
                }
            }
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
        return type.toString();
    }
    
}
