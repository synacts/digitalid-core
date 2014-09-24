package ch.virtualid.concepts;

import ch.virtualid.client.Synchronizer;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Concept;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.expression.PassiveExpression;
import ch.virtualid.handler.action.internal.AttributeValueReplace;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.both.Attributes;
import ch.xdf.Block;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * This class models an attribute with its value and visibility.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.5
 */
public final class Attribute extends Concept {
    
    // TODO: Add aspects for adding and removing attributes?
    
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
     * Stores the semantic type {@code attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("attribute@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    
    /**
     * Stores an index of attributes that are already represented by instances of this class.
     */
    private static final @Nonnull Map<Pair<Entity, SemanticType>, Attribute> index = new HashMap<Pair<Entity, SemanticType>, Attribute>();
    // TODO: Split into Map<Entity, Map<SemanticType, Attribute>> and observe the removal of roles! What about the removal of permissions?
    // TODO: This is also needed to reset all attributes of an entity.
    // TODO: Make similar changes as in the Context class.
    
    
    /**
     * Stores the type of this attribute.
     */
    private final @Nonnull SemanticType type;
    
    
    /**
     * Stores whether the value has already been loaded from the database.
     */
    private boolean valueLoaded = false;
    
    // TODO: Store the certificate as well and not just the value!
    private @Nullable SignatureWrapper certificate;
    
    /**
     * Stores the value of this attribute.
     */
    private @Nullable Block value;
    
    
    /**
     * Stores whether the unpublished value has already been loaded from the database.
     */
    private boolean unpublishedLoaded = false;
    
    /**
     * Stores the unpublished value of this attribute.
     */
    private @Nullable Block unpublished;
    
    
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
     */
    private Attribute(@Nonnull Entity entity, @Nonnull SemanticType type) {
        super(entity);
        this.type = type;
    }
    
    /**
     * Returns the attribute that represents the given type at the given entity.
     * It is guaranteed that always the same attribute is returned on the client-side.
     * 
     * @param entity the entity of the attribute to be returned.
     * @param type the type of the attribute to be returned.
     * @return the attribute that represents the given type at the given entity.
     */
    public static @Nonnull Attribute get(@Nonnull Entity entity, @Nonnull SemanticType type) {
        if (Database.isSingleAccess()) {
            synchronized(index) {
                @Nonnull Pair<Entity, SemanticType> pair = new Pair<Entity, SemanticType>(entity, type);
                @Nullable Attribute attribute = index.get(pair);
                if (attribute == null) {
                    attribute = new Attribute(entity, type);
                    index.put(pair, attribute);
                }
                return attribute;
            }
        } else {
            return new Attribute(entity, type);
        }
    }
    
    /**
     * Returns the attribute in the given block at the given entity.
     * 
     * @param entity the entity of the attribute to be returned.
     * @param block the block containing the type of the attribute.
     * 
     * @return the attribute in the given block at the given entity.
     */
    @Deprecated
    public static @Nonnull Attribute get(@Nonnull Entity entity, @Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        // TODO: Assert the block type.
        
        return get(entity, new NonHostIdentifier(block).getIdentity().toSemanticType());
    }
    
    /**
     * Returns all the attributes of the given entity.
     * 
     * @param entity the entity whose attributes are to be returned.
     * @return all the attributes of the given entity.
     */
    public static @Nonnull Set<Attribute> getAll(@Nonnull Entity entity) {
        return Attributes.getAll(entity);
    }
    
    
    /**
     * Returns the type of this attribute.
     * 
     * @return the type of this attribute.
     */
    public @Nonnull SemanticType getType() {
        return type;
    }
    
    
    /**
     * Returns the value of this attribute or null if not yet set.
     * 
     * @return the value of this attribute or null if not yet set.
     */
    public @Nullable Block getValue() throws SQLException {
        if (!valueLoaded) {
            value = Attributes.getValue(this, true);
            valueLoaded = true;
        }
        return value;
    }
    
    public void setValue(@Nullable Block value) throws SQLException {
        assert isOnClient() : "";
        
        // The old and the new value is not identical (and particularly not both null).
        if (this.value != value) { // TODO: Rather a precondition? -> No, rather a real equals().
            Synchronizer.execute(new AttributeValueReplace(this, true, getValue(), value));
        }
    }
    
    public void replaceValue(@Nonnull Block oldValue, @Nonnull Block newValue) throws SQLException {
        assert oldValue != newValue : "";
        
        if (oldValue == null) Attributes.addValue(this, true, newValue);
        else if (newValue == null) Attributes.removeValue(this, true, oldValue);
        else Attributes.replaceValue(this, true, oldValue, newValue);
        
        value = newValue;
        valueLoaded = true;
        
        notify(VALUE);
    }
    
    
    /**
     * Returns the unpublished value of this attribute or null if not yet set or available.
     * 
     * @return the unpublished value of this attribute or null if not yet set or available.
     */
    public @Nullable Block getUnpublishedValue() {
        if (!unpublishedLoaded) {
            unpublished = Attributes.getValue(this);
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
            visibility = Attributes.getValue(this);
            visibilityLoaded = true;
        }
        return visibility;
    }
    
    
    @Override
    public @Nonnull String toString() {
        return type.toString();
    }
    
}
