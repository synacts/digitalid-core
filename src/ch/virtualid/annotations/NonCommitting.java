package ch.virtualid.annotations;

import ch.virtualid.handler.Action;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Query;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that a method does not {@link Database#commit() commit} the current transaction.
 * In order to being able to rollback the whole {@link Method method}, {@link Action actions} and {@link Query queries} should never commit.
 * 
 * @see Committing
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@Documented
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface NonCommitting {}
