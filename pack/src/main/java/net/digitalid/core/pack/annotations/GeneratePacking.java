package net.digitalid.core.pack.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.meta.Interceptor;
import net.digitalid.utility.generator.information.method.MethodInformation;
import net.digitalid.utility.generator.interceptor.MethodInterceptor;
import net.digitalid.utility.processor.generator.JavaFileGenerator;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.Packable;

/**
 * This method interceptor generates the implementation of {@link Packable#pack()}.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(GeneratePacking.Interceptor.class)
public @interface GeneratePacking {
    
    /**
     * This class generates the interceptor for the surrounding annotation.
     */
    @Stateless
    public static class Interceptor extends MethodInterceptor {
        
        @Pure
        @Override
        protected void implementInterceptorMethod(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull MethodInformation method, @Nonnull String statement, @Nullable String resultVariable, @Nullable String defaultValue) {
            javaFileGenerator.addStatement("return " + javaFileGenerator.importIfPossible(Pack.class) + ".pack(this, " + method.getContainingType().asElement().getSimpleName() + "Converter.INSTANCE)");
        }
        
    }
    
}
