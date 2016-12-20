package net.digitalid.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.collections.set.FreezableLinkedHashSetBuilder;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.core.expression.operators.BinaryOperator;
import net.digitalid.core.expression.operators.RestrictionOperator;
import net.digitalid.core.identification.annotations.type.kind.AttributeType;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.node.contact.Contact;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.credentials.CredentialsSignature;

/**
 * This class models restriction expressions.
 * 
 * @invariant (operator == null) == (string == null) : "Either both the operator and the string are null or none of them.";
 */
@Immutable
@GenerateSubclass
abstract class RestrictionExpression extends Expression {
    
    /* -------------------------------------------------- Valid -------------------------------------------------- */
    
    /**
     * Returns whether the given string is valid.
     */
    @Pure
    boolean isValid(@Nonnull String string) {
        return ExpressionParser.isQuoted(string) || string.matches("\\d+");
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the attribute type for the restriction.
     */
    @Pure
    abstract @Nonnull @AttributeType SemanticType getType();
    
    /**
     * Returns the operator for the restriction.
     */
    @Pure
    abstract @Nullable RestrictionOperator getOperator();
    
    /**
     * Returns the string for the restriction.
     */
    @Pure
    abstract @Nullable @Valid String getString();
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        Validate.that((getOperator() == null) == (getString() == null)).orThrow("Either both the operator and the string have to be null or none of them.");
        super.validate();
    }
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    @Pure
    @Override
    boolean isPublic() {
        return false;
    }
    
    @Pure
    @Override
    boolean isActive() {
        return false;
    }
    
    @Pure
    @Override
    boolean isImpersonal() {
        return true;
    }
    
    /* -------------------------------------------------- Aggregations -------------------------------------------------- */
    
    @Pure
    @Override
    @Capturable @Nonnull @NonFrozen FreezableSet<@Nonnull Contact> getContacts() {
        Require.that(isActive()).orThrow("This expression has to be active but was $.", this);
        
        return FreezableLinkedHashSetBuilder.build();
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull Pack attributeContent) {
        Require.that(isImpersonal()).orThrow("This expression is impersonal.");
        
        if (!attributeContent.getType().equals(getType())) { return false; }
        
        final @Nullable RestrictionOperator operator = getOperator();
        final @Nullable String string = getString();
        
        if (operator == null || string == null) { return true; }
        
        if (ExpressionParser.isQuoted(string)) {
            final @Nonnull String substring = ExpressionParser.removeQuotes(string).toLowerCase();
            final @Nonnull String attribute;
            try {
                final @Nullable String content = attributeContent.unpack(StringConverter.INSTANCE, null);
                attribute = content != null ? content.toLowerCase() : "";
            } catch (@Nonnull ExternalException exception) {
                throw new RuntimeException(exception); // TODO
            }
            switch (operator) {
                case EQUAL: return attribute.equals(substring);
                case UNEQUAL: return !attribute.equals(substring);
                case LESS: return attribute.compareTo(substring) < 0;
                case GREATER: return attribute.compareTo(substring) > 0;
                case LESS_OR_EQUAL: return attribute.compareTo(substring) <= 0;
                case GREATER_OR_EQUAL: return attribute.compareTo(substring) >= 0;
                case PREFIX: return attribute.startsWith(substring);
                case NOT_PREFIX: return !attribute.startsWith(substring);
                case INFIX: return attribute.contains(substring);
                case NOT_INFIX: return !attribute.contains(substring);
                case POSTFIX: return attribute.endsWith(substring);
                case NOT_POSTFIX: return !attribute.endsWith(substring);
            }
        } else {
            // TODO: Use a NumberConverter to retrieve the value from the attribute content:
//            final int length = attributeContent.getLength();
//            if (length > 8) { return false; }
            long attribute = 0;
//            for (int i = 0; i < length; i++) {
//                attribute = (attribute << 8) | (attributeContent.getByte(i) & 0xFF);
//            }
            try {
                final long number = Long.parseLong(string);
                switch (operator) {
                    case EQUAL: return attribute == number;
                    case UNEQUAL: return attribute != number;
                    case LESS: return attribute < number;
                    case GREATER: return attribute > number;
                    case LESS_OR_EQUAL: return attribute <= number;
                    case GREATER_OR_EQUAL: return attribute >= number;
                }
            } catch (@Nonnull NumberFormatException exception) {}
        }
        
        return false;
    }
    
    @Pure
    @Override
    @TODO(task = "Implement the check.", date = "2016-12-02", author = Author.KASPAR_ETTER, priority = Priority.HIGH)
    boolean matches(@Nonnull CredentialsSignature<?> signature) {
        return true;
//        final @Nullable Block attributeContent = signature.getAttributeContent(type);
//        if (attributeContent == null) { return false; }
//        return matches(attributeContent);
    }
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    @Pure
    @Override
    @Nonnull String toString(@Nullable BinaryOperator operator, boolean right) {
        return ExpressionParser.addQuotesIfNecessary(getType()) + (getOperator() == null ? "" : getOperator().getSymbol()) + (getString() == null ? "" : getString());
    }
    
}
