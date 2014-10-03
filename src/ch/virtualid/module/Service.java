package ch.virtualid.module;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Site;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyArray;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Every service has to extend this class.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Service extends BothModule implements Immutable {
    
    /**
     * Stores the name of this service.
     */
    private final @Nonnull String name;
    
    /**
     * Stores the version of this service.
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
    @Pure
    public final @Nonnull String getName() {
        return name;
    }
    
    /**
     * Returns the version of this service.
     * 
     * @return the version of this service.
     */
    @Pure
    public final @Nonnull String getVersion() {
        return version;
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return name + " (" + version + ")";
    }
    
    
    @Override
    protected final void createTables(@Nonnull Site site) throws SQLException {
        throw new ShouldNeverHappenError("The method 'createTables' should never be called on a service.");
    }
    
    
    /**
     * Stores the modules that represent an entity's state in the specified order.
     */
    private final @Nonnull FreezableList<BothModule> modules = new FreezableLinkedList<BothModule>();
    
    /**
     * Returns the list of both modules that belong to this service.
     * 
     * @return the list of both modules that belong to this service.
     */
    public final @Nonnull ReadonlyList<BothModule> getModules() {
        return modules;
    }
    
    /**
     * Adds the given module to the tuple of modules.
     * 
     * @param module the module to add to the tuple of modules.
     */
    protected final void addToTuple(@Nonnull BothModule module) {
        modules.add(module);
    }
    
    @Pure
    @Override
    public final @Nonnull Block getAll(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        final int size = modules.size();
        final @Nonnull FreezableArray<Block> blocks = new FreezableArray<Block>(size);
        for (int i = 0; i < size; i++) blocks.set(i, modules.get(i).getAll(entity, agent));
        return new TupleWrapper(getType(), blocks.freeze()).toBlock();
    }
    
    @Override
    public final void addAll(@Nonnull Entity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
        
        final int size = modules.size();
        final @Nonnull ReadonlyArray<Block> blocks = new TupleWrapper(block).getElementsNotNull(size);
        for (int i = 0; i < size; i++) modules.get(i).addAll(entity, blocks.getNotNull(i));
    }
    
    @Override
    public final void removeAll(@Nonnull Entity entity) throws SQLException {
      for (final @Nonnull BothModule module : modules) module.removeAll(entity);
    }
    
}
