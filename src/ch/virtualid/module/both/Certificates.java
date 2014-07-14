package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Site;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.Module;
import ch.xdf.Block;
import ch.xdf.TupleWrapper;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the certificates of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Certificates extends BothModule {
    
    /**
     * Stores the semantic type {@code certificates.module@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("certificates.module@virtualid.ch").load(TupleWrapper.TYPE, );
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    static { Module.add(new Certificates()); }
    
    @Override
    protected void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS certificate (issuer BIGINT NOT NULL, recipient BIGINT NOT NULL, type BIGINT NOT NULL, value LONGBLOB NOT NULL, issuance BIGINT, PRIMARY KEY (issuer, recipient, type), FOREIGN KEY (issuer) REFERENCES map_identity (identity), FOREIGN KEY (recipient) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
        }
        
        Mapper.addReference("certificate", "recipient");
    }
    
    
    static void getCertificate() {
        // TODO
    }
    
    
    /**
     * Returns the state of the given entity restricted by the authorization of the given agent.
     * 
     * @param entity the entity whose state is to be returned.
     * @param agent the agent whose authorization restricts the returned state.
     * @return the state of the given entity restricted by the authorization of the given agent.
     */
    @Override
    protected @Nonnull Block getAll(@Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        return Block.EMPTY;
    }
    
    /**
     * Adds the state in the given block to the given entity.
     * 
     * @param entity the entity to which the state is to be added.
     * @param block the block containing the state to be added.
     */
    @Override
    protected void addAll(@Nonnull Entity entity, @Nonnull Block block) throws SQLException {
        
    }
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param entity the entity whose entries are to be removed.
     */
    @Override
    protected void removeAll(@Nonnull Entity entity) throws SQLException {
        
    }
    
}
