package ch.virtualid.module;

import ch.virtualid.agent.Agent;
import ch.virtualid.concept.Entity;
import ch.virtualid.database.Entity;
import ch.virtualid.identity.Mapper;
import ch.xdf.Block;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 * This class provides database access to the certificates of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Certificates extends Module {
    
    /**
     * Initializes the database by creating the appropriate tables if necessary.
     * 
     * @param connection an open client or host connection to the database.
     */
    Certificates(@Nonnull Entity connection) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS certificate (issuer BIGINT NOT NULL, recipient BIGINT NOT NULL, type BIGINT NOT NULL, value LONGBLOB NOT NULL, issuance BIGINT, PRIMARY KEY (issuer, recipient, type), FOREIGN KEY (issuer) REFERENCES map_identity (identity), FOREIGN KEY (recipient) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
        }
        
        Mapper.addIdentityReference("certificate", "recipient");
        Mapper.addTypeReference("certificate", "type");
    }
    
    static void getCertificate() {
        // TODO
    }
    
    
    /**
     * Returns the state of the given entity restricted by the authorization of the given agent.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose state is to be returned.
     * @param agent the agent whose authorization restricts the returned state.
     * @return the state of the given entity restricted by the authorization of the given agent.
     */
    @Override
    protected @Nonnull Block getAll(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull Agent agent) throws SQLException {
        return Block.EMPTY;
    }
    
    /**
     * Adds the state in the given block to the given entity.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which the state is to be added.
     * @param block the block containing the state to be added.
     */
    @Override
    protected void addAll(@Nonnull Entity connection, @Nonnull Entity entity, @Nonnull Block block) throws SQLException {
        
    }
    
    /**
     * Removes all the entries of the given entity in this module.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity whose entries are to be removed.
     */
    @Override
    protected void removeAll(@Nonnull Entity connection, @Nonnull Entity entity) throws SQLException {
        
    }
    
}
