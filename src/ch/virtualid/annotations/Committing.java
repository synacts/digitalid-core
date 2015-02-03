package ch.virtualid.annotations;

import ch.virtualid.database.Database;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that a method ends in a {@link Database#commit() committed} state if no exception is thrown.
 * Otherwise, the current transaction has to be rollbacked by the caller of the method (in case there is one).
 * 
 * @see DoesNotCommit
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@Documented
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface Committing {}
