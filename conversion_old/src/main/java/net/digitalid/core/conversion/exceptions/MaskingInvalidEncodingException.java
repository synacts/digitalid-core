package net.digitalid.core.conversion.exceptions;

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
    protected MaskingInvalidEncodingException(Exception cause) {
        super(cause.getClass().getSimpleName() + " is masked as an invalid encoding exception.", cause);
    }
    
    /**
     * Returns a new masking invalid encoding exception that masks the given cause.
     * 
     * @param cause the cause to be masked as a masking invalid encoding exception.
     * 
     * @return a new masking invalid encoding exception that masks the given cause.
     */
    public static MaskingInvalidEncodingException get(Exception cause) {
        return new MaskingInvalidEncodingException(cause);
    }
    
}
