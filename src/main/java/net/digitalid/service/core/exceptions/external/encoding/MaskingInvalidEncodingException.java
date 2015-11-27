package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.system.auxiliary.StringUtility;

/**
 * This exception allows to mask other exceptions as an invalid encoding exception.
 */
public class MaskingInvalidEncodingException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new masking invalid encoding exception that masks the given cause.
     * 
     * @param cause the cause to be masked as a masking invalid encoding exception.
     */
    protected MaskingInvalidEncodingException(@Nonnull Exception cause) {
        super(StringUtility.prependWithIndefiniteArticle(cause.getClass().getSimpleName(), true) + " is masked as an invalid encoding exception.", cause);
    }
    
    /**
     * Returns a new masking invalid encoding exception that masks the given cause.
     * 
     * @param cause the cause to be masked as a masking invalid encoding exception.
     * 
     * @return a new masking invalid encoding exception that masks the given cause.
     */
    @Pure
    public static @Nonnull MaskingInvalidEncodingException get(@Nonnull Exception cause) {
        return new MaskingInvalidEncodingException(cause);
    }
    
}
