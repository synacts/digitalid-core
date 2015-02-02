package ch.virtualid.attribute;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.SemanticType;
import ch.xdf.StringWrapper;
import javax.annotation.Nonnull;

/**
 * This class stores commonly used attribute types.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class AttributeType {
    
    /**
     * Stores the semantic type {@code name@virtualid.ch}.
     */
    public static final @Nonnull SemanticType NAME = SemanticType.create("name@virtualid.ch").load(new Category[] {Category.SYNTACTIC_TYPE, Category.SEMANTIC_TYPE, Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.MONTH, StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code prename@virtualid.ch}.
     */
    public static final @Nonnull SemanticType PRENAME = SemanticType.create("prename@virtualid.ch").load(new Category[] {Category.NATURAL_PERSON}, Time.MONTH, StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code surname@virtualid.ch}.
     */
    public static final @Nonnull SemanticType SURNAME = SemanticType.create("surname@virtualid.ch").load(new Category[] {Category.NATURAL_PERSON}, Time.MONTH, StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code email@virtualid.ch}.
     */
    public static final @Nonnull SemanticType EMAIL = SemanticType.create("email@virtualid.ch").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.HALF_DAY, StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code phone@virtualid.ch}.
     */
    public static final @Nonnull SemanticType PHONE = SemanticType.create("phone@virtualid.ch").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.HALF_DAY, StringWrapper.TYPE);
    
}
