package ch.virtualid.server;

import javax.annotation.Nonnull;

/**
 * Every service has to extend this class and provide a default constructor.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Service {
    
    /**
     * Stores the name of the service.
     */
    private final @Nonnull String name;
    
    /**
     * Stores the version of the service.
     */
    private final @Nonnull String version;
    
    /**
     * Creates a new service with the given name and version.
     * 
     * @param name the name of the service.
     * @param version the version of the service.
     */
    protected Service(@Nonnull String name, @Nonnull String version) {
        this.name = name;
        this.version = version;
    }
    
    /**
     * Returns the name of this service.
     * 
     * @return the name of this service.
     */
    public @Nonnull String getName() {
        return name;
    }
    
    /**
     * Returns the version of this service.
     * 
     * @return the version of this service.
     */
    public @Nonnull String getVersion() {
        return version;
    }
    
    @Override
    public final @Nonnull String toString() {
        return name + " (" + version + ")";
    }
    
}
