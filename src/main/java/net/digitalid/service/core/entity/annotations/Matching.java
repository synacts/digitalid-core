package net.digitalid.service.core.entity.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.handler.Handler;
import net.digitalid.utility.annotations.meta.TargetType;

/**
 * This annotation indicates that a value matches its surrounding object (usually meaning that it has the same {@link Entity entity}).
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@TargetType({Concept.class, Handler.class})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Matching {}
