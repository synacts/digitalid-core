package net.digitalid.service.core.converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.Convertible;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.xdf.XDF;

import org.junit.Test;

public class XDFConverterSingleFieldConvertibleTypeTest {
    
    private static class Foo implements Convertible {
       
        private final Boolean flag;
        
        private Foo(Boolean flag) {
           this.flag = flag; 
        }
        
        @Recover
        public static @Nonnull get(@Nullable Boolean flag) {
            return new Foo(flag);
        }
    }
    
    @Test
    public void testConvertFoo() {
        final @Nonnull Foo foo = Foo.get(true); 
        final @Nonnull Block encodedFoo = XDF.convertNonNullable(foo, Foo.class);
    }
}
