package net.digitalid.core.conversion.converter;

import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.logging.exceptions.io.StreamException;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.conversion.collector.XDFValueCollector;
import net.digitalid.core.conversion.recovery.XDFSelectionResult;

/**
 *
 */
@Stateless
public class XDF {
    
    @Pure
    public static <T> void convert(@Nonnull T object, @Nonnull Converter<T, ?> converter, @Nonnull @Modified OutputStream outputStream) throws ExternalException {
        final @Nonnull XDFValueCollector valueCollector = XDFValueCollector.with(outputStream);
        converter.convert(object, valueCollector);
    }
    
    @Pure
    public static <T> T recover(@Nonnull Converter<T, ?> converter, @Nonnull InputStream inputStream) throws ExternalException {
        final @Nonnull XDFSelectionResult selectionResult = XDFSelectionResult.with(inputStream);
        return converter.recover(selectionResult, null);
    }
    
}
