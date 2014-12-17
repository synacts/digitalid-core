package ch.virtualid.expression;

import ch.virtualid.credential.Credential;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.xdf.Block;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.StringWrapper;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * This class models restriction expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.8
 */
final class RestrictionExpression extends Expression {

    /**
     * Stores the symbols for restricting attribute values.
     */
    private final @Nonnull List<String> symbols = Arrays.asList("=", "≠", "<", ">", "≤", "≥", "/", "!/", "|", "!|", "\\", "!\\");

    /**
     * Stores the attribute type for the restriction or zero for public access.
     */
    private final long type;

    /**
     * Stores the string for the restriction.
     */
    private final String string;

    /**
     * Stores the symbol of this expression.
     */
    private final String symbol;

    /**
     * Creates a new restriction expression with the given type, string and symbol.
     * 
     * @param type the attribute type for the restriction or zero for public access.
     * @param string the string for the restriction.
     * @param symbol the symbol of this expression.
     * 
     * @require type == 0 || Mapper.isVid(type) && Category.isSemanticType(type) : "The second number is zero or denote a semantic type.";
     * @require (string == null) == (symbol == null) : "Either both string and symbol are null or none of them.";
     * @require string == null || string.startsWith("\"") && string.endsWith("\"") || string.matches("\\d+") : "If not null, the string either is a string or a number.";
     * @require symbol == null || symbols.contains(symbol) : "If not null, the symbol is valid.";
     */
    RestrictionExpression(long type, String string, String symbol) throws SQLException {
        super(null, null, 0);

        assert type == 0 || Mapper.isVid(type) && Category.isSemanticType(type) : "The second number is zero or denote a semantic type.";
        assert (string == null) == (symbol == null) : "Either both string and symbol are null or none of them.";
        assert string == null || string.startsWith("\"") && string.endsWith("\"") || string.matches("\\d+") : "If not null, the string either is a string or a number.";
        assert symbol == null || symbols.contains(symbol) : "If not null, the symbol is valid.";

        this.type = type;
        this.string = string;
        this.symbol = symbol;
    }

    /**
     * Returns whether this expression is active.
     * 
     * @return whether this expression is active.
     */
    @Override
    public boolean isActive() {
        return false;
    }

    /**
     * TODO!
     * 
     * @return whether it matches.
     * 
     * @require value != null : "The value is not null.";
     */
    private boolean match(Block value) throws InvalidEncodingException {
        assert value != null : "The value is not null.";
        
        if (string == null) return true;
        
        if (string.startsWith("\"") && string.endsWith("\"")) {
            String substring = this.string.substring(1, this.string.length() - 1).toLowerCase();
            String attribute = new StringWrapper(value).getString().toLowerCase();
            if (symbol.equals("=")) return attribute.equals(substring);
            if (symbol.equals("≠")) return !attribute.equals(substring);
            if (symbol.equals("<")) return attribute.compareTo(substring) < 0;
            if (symbol.equals(">")) return attribute.compareTo(substring) > 0;
            if (symbol.equals("≤")) return attribute.compareTo(substring) <= 0;
            if (symbol.equals("≥")) return attribute.compareTo(substring) >= 0;
            if (symbol.equals("/")) return attribute.startsWith(substring);
            if (symbol.equals("!/")) return !attribute.startsWith(substring);
            if (symbol.equals("|")) return attribute.contains(substring);
            if (symbol.equals("!|")) return !attribute.contains(substring);
            if (symbol.equals("\\")) return attribute.endsWith(substring);
            if (symbol.equals("!\\")) return !attribute.endsWith(substring);
        } else {
            long number = Long.parseLong(this.string);
            long attribute = Long.parseLong(new StringWrapper(value).getString());
            if (symbol.equals("=")) return attribute == number;
            if (symbol.equals("≠")) return attribute != number;
            if (symbol.equals("<")) return attribute < number;
            if (symbol.equals(">")) return attribute > number;
            if (symbol.equals("≤")) return attribute <= number;
            if (symbol.equals("≥")) return attribute >= number;
            throw new InvalidEncodingException("The symbol '" + symbol + "' cannot be used for numbers.");
        }
        
        return false;
    }
    
    /**
     * Returns whether this expression matches the given block (for certification restrictions).
     * 
     * @param attribute the attribute to check.
     * @return whether this expression matches the given block.
     * @require attribute != null : "The attribute is not null.";
     * @require type != 0 : "The type is not zero.";
     */
    @Override
    public boolean matches(Block attribute) throws InvalidEncodingException, Exception {
        assert attribute != null : "The attribute is not null.";
        assert type != 0 : "The type is not zero.";

        SelfcontainedWrapper selfcontainedWrapper = new SelfcontainedWrapper(attribute);
        long type = Mapper.getVid(selfcontainedWrapper.getIdentifier());

        return this.type == type && match(selfcontainedWrapper.getElement());
    }

    /**
     * Returns whether this expression matches the given credentials.
     * 
     * @param credentials the credentials to check.
     * @return whether this expression matches the given credentials.
     */
    @Override
    public boolean matches(Credential[] credentials) throws SQLException, Exception {
        // Public access with the keyword 'everybody'.
        if (type == 0) return true;

        if (credentials == null) return false;

        for (Credential credential : credentials) {
            if (credential.getAttribute() != null && Mapper.getVid(credential.getIdentifier()) == type) return matches(credential.getAttribute());
        }

        return false;
    }

    /**
     * Returns this expression as a string.
     * 
     * @return this expression as a string.
     */
    @Override
    public String toString() {
        try { return (type == 0 ? "everybody" : Mapper.getIdentifier(type)) + (symbol == null ? "" : symbol) + (string == null ? "" : string); } catch (SQLException exception) { return "ERROR" + symbol + string; }
    }
}
