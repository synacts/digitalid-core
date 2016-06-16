package net.digitalid.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashSet;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.signature.CredentialsSignatureWrapper;
import net.digitalid.service.core.block.wrappers.value.string.StringWrapper;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.identity.SemanticType;

/**
 * This class models restriction expressions.
 * 
 * @invariant (string == null) == (symbol == null) : "Either both string and symbol are null or none of them.";
 */
@Immutable
final class RestrictionExpression extends Expression {
    
    /**
     * Stores the attribute type for the restriction.
     * 
     * @invariant type.isAttributeType() : "The type denotes an attribute.";
     */
    private final @Nonnull SemanticType type;
    
    /**
     * Stores the string for the restriction.
     * 
     * @invariant string == null || isQuoted(string) || string.matches("\\d+") : "If not null, the string either is a string or a number.";
     */
    private final @Nullable String string;
    
    /**
     * Stores the symbol of this expression.
     * 
     * @invariant symbol == null || symbols.contains(symbol) : "If not null, the symbol is valid.";
     */
    private final @Nullable String symbol;
    
    /**
     * Creates a new restriction expression with the given type, string and symbol.
     * 
     * @param type the attribute type for the restriction or zero for public access.
     * @param string the string for the restriction.
     * @param symbol the symbol of this expression.
     * 
     * @require type.isAttributeType() : "The type denotes an attribute.";
     * @require (string == null) == (symbol == null) : "Either both string and symbol are null or none of them.";
     * @require string == null || isQuoted(string) || string.matches("\\d+") : "If not null, the string either is a string or a number.";
     * @require symbol == null || symbols.contains(symbol) : "If not null, the symbol is valid.";
     */
    RestrictionExpression(@Nonnull NonHostEntity entity, @Nonnull SemanticType type, @Nullable String string, @Nullable String symbol) {
        super(entity);
        
        Require.that(type.isAttributeType()).orThrow("The type denotes an attribute.");
        Require.that((string == null) == (symbol == null)).orThrow("Either both string and symbol are null or none of them.");
        Require.that(string == null || isQuoted(string) || string.matches("\\d+")).orThrow("If not null, the string either is a string or a number.");
        Require.that(symbol == null || symbols.contains(symbol)).orThrow("If not null, the symbol is valid.");
        
        this.type = type;
        this.string = string;
        this.symbol = symbol;
    }
    
    
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
    
    
    @Pure
    @Override
    @Nonnull @Capturable FreezableSet<Contact> getContacts() {
        Require.that(isActive()).orThrow("This expression is active.");
        
        return new FreezableLinkedHashSet<>();
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull Block attributeContent) {
        Require.that(isImpersonal()).orThrow("This expression is impersonal.");
        
        if (!attributeContent.getType().equals(type)) { return false; }
        
        if (string == null || symbol == null) { return true; }
        
        if (isQuoted(string)) {
            final byte[] bytes = attributeContent.getBytes(1);
            final @Nonnull String substring = removeQuotes(string).toLowerCase();
            final @Nonnull String attribute = new String(bytes, 0, bytes.length, StringWrapper.CHARSET).toLowerCase();
            if (symbol.equals("=")) { return attribute.equals(substring); }
            if (symbol.equals("≠")) { return !attribute.equals(substring); }
            if (symbol.equals("<")) { return attribute.compareTo(substring) < 0; }
            if (symbol.equals(">")) { return attribute.compareTo(substring) > 0; }
            if (symbol.equals("≤")) { return attribute.compareTo(substring) <= 0; }
            if (symbol.equals("≥")) { return attribute.compareTo(substring) >= 0; }
            if (symbol.equals("/")) { return attribute.startsWith(substring); }
            if (symbol.equals("!/")) { return !attribute.startsWith(substring); }
            if (symbol.equals("|")) { return attribute.contains(substring); }
            if (symbol.equals("!|")) { return !attribute.contains(substring); }
            if (symbol.equals("\\")) { return attribute.endsWith(substring); }
            if (symbol.equals("!\\")) { return !attribute.endsWith(substring); }
        } else {
            final int length = attributeContent.getLength();
            if (length > 8) { return false; }
            long attribute = 0;
            for (int i = 0; i < length; i++) {
                attribute = (attribute << 8) | (attributeContent.getByte(i) & 0xFF);
            }
            try {
                final long number = Long.parseLong(this.string);
                if (symbol.equals("=")) { return attribute == number; }
                if (symbol.equals("≠")) { return attribute != number; }
                if (symbol.equals("<")) { return attribute < number; }
                if (symbol.equals(">")) { return attribute > number; }
                if (symbol.equals("≤")) { return attribute <= number; }
                if (symbol.equals("≥")) { return attribute >= number; }
            } catch (@Nonnull NumberFormatException exception) {}
        }
        
        return false;
    }
    
    @Pure
    @Override
    boolean matches(@Nonnull CredentialsSignatureWrapper signature) {
        final @Nullable Block attributeContent = signature.getAttributeContent(type);
        if (attributeContent == null) { return false; }
        return matches(attributeContent);
    }
    
    
    @Pure
    @Override
    @Nonnull String toString(@Nullable Character operator, boolean right) {
        Require.that(operator == null || operators.contains(operator)).orThrow("The operator is valid.");
        
        return addQuotesIfNecessary(type) + (symbol == null ? "" : symbol) + (string == null ? "" : string);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof RestrictionExpression)) { return false; }
        final @Nonnull RestrictionExpression other = (RestrictionExpression) object;
        return this.type.equals(other.type) && this.string.equals(other.string) && this.symbol.equals(other.symbol);
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + type.hashCode();
        hash = 19 * hash + string.hashCode();
        hash = 19 * hash + symbol.hashCode();
        return hash;
    }
    
}
