package net.digitalid.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.core.database.Database;

/**
 * This annotation indicates that a method ends in a {@link Database#commit() committed} state if no exception is thrown.
 * Otherwise, the current transaction has to be rollbacked by the caller of the method (in case there is one).
 * 
 * @see NonCommitting
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Committing {}
