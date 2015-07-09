package net.digitalid.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import net.digitalid.core.collections.FreezableCollection;
import net.digitalid.core.collections.ReadOnlyCollection;

/**
 * This annotation indicates that a {@link Collection collection} contains a {@link ReadonlyCollection#isSingle() single} element.
 * 
 * @see NonSingle
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@TargetType({Collection.class, FreezableCollection.class})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Single {}
