package net.digitalid.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.signature.CredentialsSignatureWrapper;
import net.digitalid.service.core.concept.NonHostConcept;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.concepts.contact.Context;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;
import net.digitalid.service.core.identifier.IdentifierImplementation;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.Person;
import net.digitalid.service.core.identity.SemanticType;

/**
 * This class parses and represents expressions.
 * 
 * @see BinaryExpression
 * @see ContactExpression
 * @see ContextExpression
 * @see EmptyExpression
 * @see EverybodyExpression
 * @see RestrictionExpression
 */
@Immutable
abstract class Expression extends NonHostConcept {
    
    /**
     * Creates a new expression with the given entity.
     * 
     * @param entity the entity to which this expression belongs.
     */
    Expression(@Nonnull NonHostEntity entity) {
        super(entity);
    }
    
    
    /**
     * Returns whether this expression is public.
     * 
     * @return whether this expression is public.
     */
    @Pure
    abstract boolean isPublic();
    
    /**
     * Returns whether this expression is active.
     * 
     * @return whether this expression is active.
     */
    @Pure
    abstract boolean isActive();
    
    /**
     * Returns whether this expression is impersonal.
     * 
     * @return whether this expression is impersonal.
     */
    @Pure
    abstract boolean isImpersonal();
    
    
    /**
     * Returns the contacts denoted by this expression.
     * 
     * @return the contacts denoted by this expression.
     * 
     * @require isActive() : "This expression is active.";
     */
    @Pure
    @NonCommitting
    abstract @Nonnull @Capturable FreezableSet<Contact> getContacts() throws DatabaseException;
    
    /**
     * Returns whether this expression matches the given attribute content.
     * 
     * @param attributeContent the attribute content which is to be checked.
     * 
     * @return whether this expression matches the given attribute content.
     * 
     * @require isImpersonal() : "This expression is impersonal.";
     */
    @Pure
    abstract boolean matches(@Nonnull Block attributeContent);
    
    /**
     * Returns whether this expression matches the given signature.
     * 
     * @param signature the signature which is to be checked.
     * 
     * @return whether this expression matches the given signature.
     */
    @Pure
    @NonCommitting
    abstract boolean matches(@Nonnull CredentialsSignatureWrapper signature) throws DatabaseException;
    
    
    /**
     * Returns this expression as a string.
     * 
     * @param operator the operator of the parent binary expression or null otherwise.
     * @param right whether this expression is the right child of a binary expression.
     * 
     * @return this expression as a string.
     * 
     * @require operator == null || operators.contains(operator) : "The operator is valid.";
     */
    @Pure
    abstract @Nonnull String toString(@Nullable Character operator, boolean right);
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return toString(null, false);
    }
    
    
    @Pure
    @Override
    public abstract boolean equals(@Nullable Object object);
    
    @Pure
    @Override
    public abstract int hashCode();
    
    
    /**
     * Stores the characters for addition and subtraction.
     */
    static final @Nonnull ReadOnlyList<Character> addition = FreezableArrayList.get('+', '-').freeze();
    
    /**
     * Stores the character for multiplication.
     */
    static final @Nonnull ReadOnlyList<Character> multiplication = new FreezableArrayList<Character>('*').freeze();
    
    /**
     * Stores the characters for all binary operators.
     */
    static final @Nonnull ReadOnlyList<Character> operators = FreezableArrayList.get('+', '-', '*').freeze();
    
    /**
     * Stores the symbols for restriction.
     */
    static final @Nonnull ReadOnlyList<String> symbols = FreezableArrayList.get("=", "≠", "<", ">", "≤", "≥", "/", "!/", "|", "!|", "\\", "!\\").freeze();
    
    /**
     * Returns the last index of one of the given characters in the given string considering quotation marks and parentheses.
     * 
     * @param string the string to parse.
     * @param characters the characters to look for.
     * 
     * @return the last index of one of the given characters in the given string considering quotation marks and parentheses.
     */
    @Pure
    private static int lastIndexOf(@Nonnull String string, @Nonnull ReadOnlyList<Character> characters) throws InvalidEncodingException, InternalException {
        int parenthesesCounter = 0;
        boolean quotation = false;
        
        final int length = string.length();
        for (int i = length - 1; i >= 0; i--) {
            final char c = string.charAt(i);
            
            // Check if the char is in a quotation.
            if (quotation) {
                if (c == '\"') { quotation = false; }
                continue;
            } else if (c == '\"') {
                quotation = true;
                continue;
            }
            
            // Check for parentheses.
            if (c == ')') {
                parenthesesCounter++;
                continue;
            } else if (c == '(') {
                if (parenthesesCounter == 0) { throw InvalidParameterValueException.get("opening parentheses", string); }
                parenthesesCounter--;
                continue;
            }
            
            if (parenthesesCounter == 0 && characters.contains(c)) { return i; }
        }
        
        if (parenthesesCounter > 0) { throw InvalidParameterValueException.get("closing parentheses", string); }
        if (quotation) { throw InvalidParameterValueException.get("closing quotation marks", string); }
        
        return -1;
    }
    
    /**
     * Returns whether the given string is quoted.
     * 
     * @param string the string to be checked.
     * 
     * @return whether the given string is quoted.
     */
    @Pure
    static boolean isQuoted(@Nonnull String string) {
        return string.startsWith("\"") && string.endsWith("\"");
    }
    
    /**
     * Removes the quotes from the given string.
     * 
     * @param string the string to be unquoted.
     * 
     * @return the given string without quotes.
     * 
     * @require isQuoted(string) : "The string is quoted.";
     */
    @Pure
    static @Nonnull String removeQuotes(@Nonnull String string) {
        Require.that(isQuoted(string)).orThrow("The string is quoted.");
        
        return string.substring(1, string.length() - 1);
    }
    
    /**
     * Adds quotes to the identifier of the given identity if necessary.
     * 
     * @param identity the identity which is to be returned as an identifier.
     * 
     * @return the identifier of the given identity with quotes if necessary.
     */
    @Pure
    static @Nonnull String addQuotesIfNecessary(@Nonnull Identity identity) {
        final @Nonnull String identifier = identity.getAddress().getString();
        return identifier.contains("-") ? "\"" + identifier + "\"" : identifier;
    }
    
    /**
     * Parses the given string for the given entity.
     * 
     * @param entity the entity of the returned expression.
     * @param string the string which is to be parsed.
     * 
     * @return the expression of the parsed string.
     */
    @Pure
    @NonCommitting
    static Expression parse(@Nonnull NonHostEntity entity, @Nonnull String string) throws ExternalException {
        if (string.trim().isEmpty()) { return new EmptyExpression(entity); }
        
        int index = lastIndexOf(string, addition);
        if (index == -1) { index = lastIndexOf(string, multiplication); }
        if (index != -1) { return new BinaryExpression(entity, string.substring(0, index), string.substring(index + 1, string.length()), string.charAt(index)); }
        
        if (string.charAt(0) == '(' && string.charAt(string.length() - 1) == ')') { return parse(entity, string.substring(1, string.length() - 1)); }
        
        // The string is now either a context, a contact or a restriction.
        
        for (final @Nonnull String symbol : symbols) {
            index = string.indexOf(symbol);
            if (index != -1) {
                @Nonnull String identifier = string.substring(0, index).trim();
                if (isQuoted(identifier)) { identifier = removeQuotes(identifier); }
                if (!IdentifierImplementation.isValid(identifier)) { throw InvalidParameterValueException.get("identifier", identifier); }
                final @Nonnull SemanticType type = IdentifierImplementation.get(identifier).getIdentity().castTo(SemanticType.class).checkIsAttributeType();
                final @Nonnull String substring = string.substring(index + symbol.length(), string.length()).trim();
                if (isQuoted(substring) || substring.matches("\\d+")) { return new RestrictionExpression(entity, type, substring, symbol); }
                else { throw InvalidParameterValueException.get("substring", substring); }
            }
        }
        
        if (string.equals("everybody")) { return new EverybodyExpression(entity); }
        
        if (string.matches("\\d+")) { return new ContextExpression(entity, Context.get(entity, string)); }
        
        final @Nonnull String identifier = isQuoted(string) ? removeQuotes(string) : string;
        if (IdentifierImplementation.isValid(string)) {
            final @Nonnull Identity identity = IdentifierImplementation.get(identifier).getIdentity();
            if (identity instanceof Person) { return new ContactExpression(entity, Contact.get(entity, (Person) identity)); }
            if (identity instanceof SemanticType) { return new RestrictionExpression(entity, ((SemanticType) identity).checkIsAttributeType(), null, null); }
            throw InvalidParameterValueException.get("identity", identifier);
        }
        
        throw InvalidParameterValueException.get("string", string);
    }
    
}
