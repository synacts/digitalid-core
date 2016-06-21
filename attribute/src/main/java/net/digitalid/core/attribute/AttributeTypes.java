package net.digitalid.core.attribute;

import javax.annotation.Nonnull;

import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.Category;

import net.digitalid.service.core.auxiliary.Time;

/**
 * This class stores commonly used attribute types.
 */
public final class AttributeTypes {
    
    /**
     * Stores the semantic type {@code name@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType NAME = SemanticType.map("name@core.digitalid.net").load(new Category[] {Category.SYNTACTIC_TYPE, Category.SEMANTIC_TYPE, Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.MONTH, StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code prename@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PRENAME = SemanticType.map("prename@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON}, Time.MONTH, StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code surname@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SURNAME = SemanticType.map("surname@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON}, Time.MONTH, StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code email@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType EMAIL = SemanticType.map("email@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.HALF_DAY, StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code phone@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PHONE = SemanticType.map("phone@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.HALF_DAY, StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code skype@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SKYPE = SemanticType.map("skype@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.HALF_DAY, StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code address@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType ADDRESS = SemanticType.map("address@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.WEEK, StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code website@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType WEBSITE = SemanticType.map("website@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.WEEK, StringWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code birthday@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType BIRTHDAY = SemanticType.map("birthday@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON}, Time.TROPICAL_YEAR, StringWrapper.XDF_TYPE);
    
}
