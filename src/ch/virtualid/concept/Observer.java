package ch.virtualid.concept;

import javax.annotation.Nonnull;

/**
 * Implementing this interface allows a class to observe changes in {@link Instance instances}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface Observer {
    
    /**
     * Notifies the object about a change in the given aspect of the given instance.
     * 
     * @param aspect the aspect that changed in the given instance.
     * @param instance the instance that reported a change in the given aspect.
     * 
     * @require aspect.getClazz().isInstance(instance) : "The instance is an instance of the aspect's class.";
     */
    public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance);
    
}
