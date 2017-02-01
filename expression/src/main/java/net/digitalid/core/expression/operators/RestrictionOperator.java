package net.digitalid.core.expression.operators;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.validation.annotations.type.Immutable;


/**
 * This class enumerates the operators that can be used in attribute restrictions.
 */
@Immutable
public enum RestrictionOperator {
    
    EQUAL("="),
    UNEQUAL("≠"),
    LESS("<"),
    GREATER(">"),
    LESS_OR_EQUAL("≤"),
    GREATER_OR_EQUAL("≥"),
    PREFIX("/"),
    NOT_PREFIX("!/"),
    INFIX("|"),
    NOT_INFIX("!|"),
    POSTFIX("\\"),
    NOT_POSTFIX("!\\");
    
    /* -------------------------------------------------- Symbol -------------------------------------------------- */
    
    private final @Nonnull String symbol;
    
    /**
     * Returns the symbol of this operator.
     */
    @Pure
    public @Nonnull String getSymbol() {
        return symbol;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private RestrictionOperator(@Nonnull String symbol) {
        this.symbol = symbol;
    }
    
    /**
     * Returns the restriction operator denoted by the given symbol.
     */
    @Pure
    public static @Nonnull RestrictionOperator of(@Nonnull String symbol) {
        for (@Nonnull RestrictionOperator operator : values()) {
            if (operator.getSymbol() == symbol) { return operator; }
        }
        
        throw CaseExceptionBuilder.withVariable("symbol").withValue(symbol).build();
    }
    
}
