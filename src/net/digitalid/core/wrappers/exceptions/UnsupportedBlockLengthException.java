package net.digitalid.core.wrappers.exceptions;

import java.io.IOException;

/**
 * Blocks that are larger than the maximum integer are not supported by this library due to the array limitations of Java.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class UnsupportedBlockLengthException extends IOException {}
