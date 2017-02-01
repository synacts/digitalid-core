package net.digitalid.core.expression;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.expression.operators.BinaryOperator;
import net.digitalid.core.expression.operators.RestrictionOperator;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.Person;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.node.contact.Contact;
import net.digitalid.core.node.context.Context;

/**
 * This class parses expressions.
 */
@Utility
abstract class ExpressionParser {
    
    /* -------------------------------------------------- Quotes -------------------------------------------------- */
    
    /**
     * Returns whether the given string is quoted.
     */
    @Pure
    static boolean isQuoted(@Nonnull String string) {
        return string.startsWith("\"") && string.endsWith("\"");
    }
    
    /**
     * Removes the quotes from the given string.
     * 
     * @require isQuoted(string) : "The string is quoted.";
     */
    @Pure
    static @Nonnull String removeQuotes(@Nonnull String string) {
        Require.that(isQuoted(string)).orThrow("The string has to be quoted but was $.", string);
        
        return string.substring(1, string.length() - 1);
    }
    
    /**
     * Adds quotes to the identifier of the given identity if necessary.
     */
    @Pure
    static @Nonnull String addQuotesIfNecessary(@Nonnull Identity identity) {
        final @Nonnull String identifier = identity.getAddress().getString();
        return identifier.contains("-") ? "\"" + identifier + "\"" : identifier;
    }
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    /**
     * Returns the last index of one of the given characters in the given string considering quotation marks and parentheses.
     */
    @Pure
    private static int lastIndexOf(@Nonnull String string, @Nonnull FiniteIterable<@Nonnull BinaryOperator> characters) throws RecoveryException {
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
                if (parenthesesCounter == 0) { throw RecoveryExceptionBuilder.withMessage("There is an opening parenthesis too much: " + string).build(); }
                parenthesesCounter--;
                continue;
            }
            
            if (parenthesesCounter == 0 && characters.map(BinaryOperator::getSymbol).contains(c)) { return i; } // TODO: Do something smarter than mapping and auto-boxing.
        }
        
        if (parenthesesCounter > 0) { throw RecoveryExceptionBuilder.withMessage("There is an opening parenthesis missing: " + string).build(); }
        if (quotation) { throw RecoveryExceptionBuilder.withMessage("The quotation marks do not match: " + string).build(); }
        
        return -1;
    }
    
    /* -------------------------------------------------- Parsing -------------------------------------------------- */
    
    /**
     * Parses the given string for the given entity.
     */
    @Pure
    @NonCommitting
    @SuppressWarnings("AssignmentToMethodParameter")
    static @Nonnull Expression parse(@Nonnull NonHostEntity entity, @Nonnull String string) throws ExternalException {
        string = string.trim();
        
        if (string.isEmpty()) { return new EmptyExpressionSubclass(); }
        
        int index = lastIndexOf(string, BinaryOperator.getOperators(0));
        if (index == -1) { index = lastIndexOf(string, BinaryOperator.getOperators(1)); }
        if (index != -1) {
            final @Nonnull Expression leftChild = parse(entity, string.substring(0, index));
            final @Nonnull Expression rightChild = parse(entity, string.substring(index + 1, string.length()));
            final @Nonnull BinaryOperator operator = BinaryOperator.of(string.charAt(index));
            return new BinaryExpressionSubclass(leftChild, rightChild, operator);
        }
        
        if (string.charAt(0) == '(' && string.charAt(string.length() - 1) == ')') { return parse(entity, string.substring(1, string.length() - 1)); }
        
        // The string is now either a context, a contact or a restriction.
        
        for (final @Nonnull RestrictionOperator operator : RestrictionOperator.values()) {
            index = string.indexOf(operator.getSymbol());
            if (index != -1) {
                @Nonnull String identifier = string.substring(0, index).trim();
                if (isQuoted(identifier)) { identifier = removeQuotes(identifier); }
                if (!Identifier.isValid(identifier)) { throw RecoveryExceptionBuilder.withMessage("The identifier is invalid: " + identifier).build(); }
                final @Nonnull SemanticType type = Identifier.with(identifier).resolve().castTo(SemanticType.class); // TODO: .checkIsAttributeType();
                final @Nonnull String substring = string.substring(index + operator.getSymbol().length(), string.length()).trim();
                if (isQuoted(substring) || substring.matches("\\d+")) { return new RestrictionExpressionSubclass(type, operator, substring); }
                else { throw RecoveryExceptionBuilder.withMessage("The substring is not a valid restriction: " + substring).build(); }
            }
        }
        
        if (string.equals("everybody")) { return new EverybodyExpressionSubclass(); }
        
        if (string.matches("\\d+")) { return new ContextExpressionSubclass(Context.of(entity, Long.parseLong(string))); }
        
        final @Nonnull String identifier = isQuoted(string) ? removeQuotes(string) : string;
        if (Identifier.isValid(string)) {
            final @Nonnull Identity identity = Identifier.with(identifier).resolve();
            if (identity instanceof Person) { return new ContactExpressionSubclass(Contact.of(entity, (Person) identity)); }
            if (identity instanceof SemanticType) { return new RestrictionExpressionSubclass(((SemanticType) identity)/* TODO: .checkIsAttributeType() */, null, null); }
            throw RecoveryExceptionBuilder.withMessage("The identity has to be either a person or a semantic type: " + identifier).build();
        }
        
        throw RecoveryExceptionBuilder.withMessage("The following string could not be parsed: " + string).build();
    }
    
}
