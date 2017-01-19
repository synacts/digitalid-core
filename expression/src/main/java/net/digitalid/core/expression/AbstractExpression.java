package net.digitalid.core.expression;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;

/**
 * This class models abstract expressions.
 * 
 * @see ActiveExpression
 * @see PassiveExpression
 * @see PersonalExpression
 * @see ImpersonalExpression
 */
@Immutable
abstract class AbstractExpression extends RootClass {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    /**
     * Returns the entity to which this expression belongs.
     */
    @Pure
    @Provided
    public abstract @Nonnull NonHostEntity getEntity();
    
    /* -------------------------------------------------- Expression -------------------------------------------------- */
    
    @Pure
    @TODO(task = "Support throwing exceptions with derived fields.", date = "2016-12-02", author = Author.KASPAR_ETTER)
    @Nonnull Expression parse(@Nonnull NonHostEntity entity, @Nonnull String string) {
        try {
            return ExpressionParser.parse(entity, string);
        } catch (@Nonnull ExternalException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    /**
     * Returns the expression of this abstract expression.
     */
    @Pure
    @Derive("parse(entity, string)")
//    @Derive("ExpressionParser.parse(entity, string)")
    abstract @Nonnull Expression getExpression();
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    /**
     * Returns the string which is to be parsed.
     */
    @Pure
    // TODO: @Normalize("expression.toString()")
    public abstract @Nonnull String getString();
    
}
