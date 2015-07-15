package net.digitalid.core.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

/**
 * This class allows the created threads to be named.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class NamedThreadFactory implements ThreadFactory {
    
    /**
     * Stores the prefix of the threads.
     */
    private final String prefix;
    
    /**
     * Stores the number of the next thread.
     */
    private final AtomicInteger number = new AtomicInteger(1);
    
    /**
     * Creates a new thread factory with the given name prefix.
     * 
     * @param prefix the prefix of the threads created by this factory.
     */
    public NamedThreadFactory(@Nonnull String prefix) {
        this.prefix = prefix + "-";
    }
    
    @Override
    public @Nonnull Thread newThread(@Nonnull Runnable runnable) {
        return new Thread(runnable, prefix + number.getAndIncrement());
    }
    
}
