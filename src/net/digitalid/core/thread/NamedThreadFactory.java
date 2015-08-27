package net.digitalid.core.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;

/**
 * This class allows the created threads to be named.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class NamedThreadFactory implements ThreadFactory {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Fields –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the prefix of the threads.
     */
    private final @Nonnull String prefix;
    
    /**
     * Stores the number of the next thread.
     */
    private final @Nonnull AtomicInteger number = new AtomicInteger(1);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new thread factory with the given name prefix.
     * 
     * @param prefix the prefix of the threads created by this factory.
     */
    protected NamedThreadFactory(@Nonnull String prefix) {
        this.prefix = prefix + "-";
    }
    
    /**
     * Returns a new thread factory with the given name prefix.
     * 
     * @param prefix the prefix of the threads created by this factory.
     * 
     * @return a new thread factory with the given name prefix.
     */
    @Pure
    public static @Nonnull NamedThreadFactory get(@Nonnull String prefix) {
        return new NamedThreadFactory(prefix);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Method –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Override
    public @Nonnull Thread newThread(@Nonnull Runnable runnable) {
        return new Thread(runnable, prefix + number.getAndIncrement());
    }
    
}
