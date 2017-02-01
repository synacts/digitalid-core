package net.digitalid.core.expression.operators;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.validation.annotations.type.Immutable;


/**
 * This class enumerates the operators that can be used in binary expressions.
 */
@Immutable
public enum BinaryOperator {
    
    /* -------------------------------------------------- Operators -------------------------------------------------- */
    
    ADDITION('+', 0),
    SUBTRACTION('-', 0),
    MULTIPLICATION('*', 1);
    
    /* -------------------------------------------------- Symbol -------------------------------------------------- */
    
    private final char symbol;
    
    /**
     * Returns the symbol of this operator.
     */
    @Pure
    public char getSymbol() {
        return symbol;
    }
    
    /* -------------------------------------------------- Order -------------------------------------------------- */
    
    private final int order;
    
    /**
     * Returns the order of this operator.
     */
    @Pure
    public int getOrder() {
        return order;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private BinaryOperator(char symbol, int order) {
        this.symbol = symbol;
        this.order = order;
    }
    
    /**
     * Returns the binary operator denoted by the given symbol.
     */
    @Pure
    public static @Nonnull BinaryOperator of(char symbol) {
        for (@Nonnull BinaryOperator operator : values()) {
            if (operator.getSymbol() == symbol) { return operator; }
        }
        
        throw CaseExceptionBuilder.withVariable("symbol").withValue(symbol).build();
    }
    
    /* -------------------------------------------------- Values -------------------------------------------------- */
    
    /**
     * Returns all operators of the given order.
     */
    @Pure
    public static @Nonnull FiniteIterable<@Nonnull BinaryOperator> getOperators(int order) {
        return FiniteIterable.of(values()).filter(operator -> operator.getOrder() == order);
    }
    
}
