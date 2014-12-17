package ch.virtualid.expression;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.NonHostConcept;
import ch.virtualid.contact.Contact;
import ch.virtualid.credential.Credential;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableSet;
import ch.xdf.Block;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * This class parses and represents expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.8
 */
abstract class Expression extends NonHostConcept implements Immutable {
    
    /**
     * Creates a new expression with the given entity.
     * 
     * @param entity the entity to which this expression belongs.
     */
    Expression(@Nonnull NonHostEntity entity) {
        super(entity);
    }
    
    
    /**
     * Returns whether this expression is active.
     * 
     * @return whether this expression is active.
     */
    abstract boolean isActive();
    
    /**
     * Returns whether this expression is impersonal.
     * 
     * @return whether this expression is impersonal.
     */
    abstract boolean isImpersonal();
    
    
    /**
     * Returns the contacts denoted by this expression.
     * 
     * @return the contacts denoted by this expression.
     * 
     * @require isActive() : "This expression is active.";
     */
    @Pure
    abstract @Nonnull @Capturable FreezableSet<Contact> getContacts();
    
    /**
     * Returns whether this expression matches the given block.
     * 
     * @param content the attribute to check.
     * 
     * @return whether this expression matches the given block.
     * 
     * @require isImpersonal() : "This expression is impersonal.";
     */
    @Pure
    abstract boolean matches(@Nonnull Block content) throws InvalidEncodingException, SQLException, Exception;
    
    /**
     * Returns whether this expression matches the given credentials.
     * 
     * @param credentials the credentials to check.
     * 
     * @return whether this expression matches the given credentials.
     */
    @Pure
    abstract boolean matches(Credential[] credentials) throws InvalidEncodingException, SQLException, Exception;
    
    
    /**
     * Parses the given string with the given host and VID, and returns an appropriate expression.
     * 
     * @param string the string to parse.
     * @param connection a connection to the database.
     * @param host the host of the VID.
     * @param vid the VID of the contexts.
     * 
     * @return the expression of the parsed string.
     */
    public static Expression parse(@Nonnull NonHostEntity entity, @Nonnull String string) throws InvalidEncodingException {
        string = string.trim();
        if (string.isEmpty()) return new EmptyExpression();
        
        int index = lastIndexOf(string, Arrays.asList('+', '-'));
        if (index == -1) index = lastIndexOf(string, Arrays.asList('*'));
        if (index != -1) return new BinaryExpression(connection, host, vid, string.substring(0, index), string.substring(index + 1, string.length()), string.charAt(index));
        
        if (string.charAt(0) == '(' && string.charAt(string.length() - 1) == ')') return parse(string.substring(1, string.length() - 1), connection, host, vid);
        
        // The string is now either a context, a contact or a restriction.
        
        String[] symbols = new String[]{"=", "≠", "<", ">", "≤", "≥", "/", "!/", "|", "!|", "\\", "!\\"};
        for (String symbol : symbols) {
            index = string.indexOf(symbol);
            if (index != -1) {
                String identifier = string.substring(0, index).trim();
                if (!Identifier.isValid(identifier)) throw new InvalidEncodingException("The string '" + string + "' does not start with a valid identifier.");
                long type = Mapper.getVid(identifier);
                if (!Category.isSemanticType(type)) throw new InvalidEncodingException("The identifier '" + identifier + "' does not denote a semantic type.");
                String substring = string.substring(index + symbol.length(), string.length()).trim();
                if (substring.startsWith("\"") && substring.endsWith("\"") || substring.matches("\\d+")) return new RestrictionExpression(type, substring, symbol);
                else throw new InvalidEncodingException("The string '" + substring + "' is neither a quoted string nor a number.");
            }
        }
        
        if (string.equals("everybody")) return new RestrictionExpression(0, null, null);
        
        if (Identifier.isValid(string)) {
            long contact = Mapper.getVid(string);
            if (Category.isPerson(contact)) return new ContactExpression(contact);
            if (Category.isSemanticType(contact)) return new RestrictionExpression(contact, null, null);
            throw new InvalidEncodingException("The string '" + string + "' is a valid identifier but neither a person nor a semantic type.");
        }
        
        if (string.matches("\\d+")) return new ContextExpression(connection, host, vid, Integer.parseInt(string));
        
        throw new InvalidEncodingException("The string '" + string + "' could not be parsed.");
    }
    
    /**
     * Returns the last index of one of the characters in the given string considering quotation marks and parentheses.
     * 
     * @param string the string to parse.
     * @param characters the characters to look for.
     * 
     * @return the last index of one of the characters in the given string considering quotation marks and parentheses.
     * 
     * @require string != null : "The string is not null.";
     * @require characters != null : "The characters is not null.";
     */
    private static int lastIndexOf(String string, List<Character> characters) throws InvalidEncodingException {
        assert string != null : "The string is not null.";
        assert characters != null : "The characters is not null.";
        
        int parenthesesCounter = 0;
        boolean quotation = false;
        
        for (int i = string.length() - 1; i >= 0; i--) {
            char c = string.charAt(i);
            
            // Check if the char is in a quotation.
            if (quotation) {
                if (c == '\"') quotation = false;
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
                if (parenthesesCounter <= 0) throw new InvalidEncodingException("The string '" + string + "' has more opening than closing parentheses.");
                parenthesesCounter--;
                continue;
            }
            
            if (parenthesesCounter == 0 && characters.contains(c)) return i;
        }
        
        if (parenthesesCounter > 0) throw new InvalidEncodingException("The string '" + string + "' has more closing than opening parentheses.");
        if (quotation) throw new InvalidEncodingException("The string '" + string + "' has more closing than opening quotation marks.");
        
        return -1;
    }
    
}
