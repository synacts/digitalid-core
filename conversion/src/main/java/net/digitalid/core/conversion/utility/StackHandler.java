package net.digitalid.core.conversion.utility;

import java.util.Stack;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;

/**
 *
 */
public abstract class StackHandler<T, W extends T> {
    
    /* -------------------------------------------------- Stack Pushing -------------------------------------------------- */
    
    /**
     * The output stream into which we are writing.
     */
    private @Nonnull Stack<@Nonnull T> stack = new Stack<>();
    
    @Pure
    private T getStackEntry() {
        return stack.peek();
    }
    
    @Pure
    @SuppressWarnings("unchecked")
    private <X extends T> @Nonnull X getStackEntry(@Nonnull Class<X> expectedType) {
        final @Nonnull T outputStream = getStackEntry();
        Require.that(expectedType.isInstance(outputStream)).orThrow("Expected stack entry of type $, but got $", expectedType, outputStream.getClass());
        
        return (X) outputStream;
    }
    
    @Pure
    protected abstract W wrapEntry(T stackEntry);
    
    /**
     * Adds a stack entry of type T to the stack and adds another stack entry of type W, which wraps the given stack entry.
     */
    @Impure
    public void addAndWrapStackEntry(T stackEntry) {
        stack.add(stackEntry);
        stack.add(wrapEntry(stackEntry));
    }
    
    /* -------------------------------------------------- Stack Popping -------------------------------------------------- */
    
    @Impure
    private <X extends T> @Nonnull X popStackEntry(@Nonnull Class<X> expectedType) {
        final @Nonnull X stackEntry = getStackEntry(expectedType);
        stack.pop();
        return stackEntry;
    }
    
    @Pure
    protected abstract @Nonnull Class<W> getWrapperType();
    
    /**
     * Pops the output stream stack twice: Once to remove the top data output stream and the second time to remove the 
     * underlying output stream, e.g. the cipher-, digest-, or deflater output stream.
     */
    @Impure
    public <X extends T> @Nonnull X popWrappedStackEntry(@Nonnull Class<X> expectedType) {
        popStackEntry(getWrapperType());
        return popStackEntry(expectedType);
    }
    
    @Pure
    @SuppressWarnings("unchecked")
    public @Nonnull W peek() {
        Require.that(getWrapperType().isInstance(getStackEntry())).orThrow("Expected wrapper type on top of the stack, but got $", getStackEntry());
        return (W) stack.peek();
    }
    
    /* -------------------------------------------------- Size -------------------------------------------------- */
    
    @Pure
    public boolean hasSize(int expectedSize) {
        return size() == expectedSize;
    }
    
    @Pure
    public int size() {
        return stack.size();
    }
    
}
