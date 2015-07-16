package net.digitalid.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import net.digitalid.core.collections.FreezableCollection;

/**
 * This annotation indicates that a method should only be invoked on {@link EmptyOrSingle empty or single} objects.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@TargetType({Collection.class, FreezableCollection.class})
public @interface EmptyOrSingleRecipient {}
