package net.digitalid.core.attribute;

import javax.annotation.Nonnull;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.identity.Category;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.StringWrapper;

/**
 * This class stores commonly used attribute types.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class AttributeType {
    
    /**
     * Stores the semantic type {@code name@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType NAME = SemanticType.create("name@core.digitalid.net").load(new Category[] {Category.SYNTACTIC_TYPE, Category.SEMANTIC_TYPE, Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.MONTH, StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code prename@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PRENAME = SemanticType.create("prename@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON}, Time.MONTH, StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code surname@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SURNAME = SemanticType.create("surname@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON}, Time.MONTH, StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code email@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType EMAIL = SemanticType.create("email@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.HALF_DAY, StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code phone@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PHONE = SemanticType.create("phone@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.HALF_DAY, StringWrapper.TYPE);
    
}
