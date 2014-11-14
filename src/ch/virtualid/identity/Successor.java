package ch.virtualid.identity;

import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidDeclarationException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.handler.query.external.IdentityQuery;
import ch.virtualid.handler.reply.query.IdentityReply;
import ch.virtualid.identifier.ExternalIdentifier;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identifier.NonHostIdentifier;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the successor of an {@link Identifier identifier}.
 * 
 * TODO: Support the export and import of all successors that belong to identifiers of a certain host.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.8
 */
public final class Successor {
    
    static {
        assert Database.isMainThread() : "This static block is called in the main thread.";
        
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS general_successor (identifier " + IdentifierClass.FORMAT + " NOT NULL, successor " + IdentifierClass.FORMAT + " NOT NULL, reply " + Reply.FORMAT + ", PRIMARY KEY (identifier), FOREIGN KEY (reply) " + Reply.REFERENCE + ")");
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("The database tables of the predecessors could not be created.", exception);
        }
    }
    
    /**
     * Returns the successor of the given identifier as stored in the database.
     * Please note that the returned successor is only claimed and not yet verified.
     * 
     * @param identifier the identifier whose successor is to be returned.
     * 
     * @return the successor of the given identifier as stored in the database.
     */
    public static @Nullable InternalNonHostIdentifier get(@Nonnull NonHostIdentifier identifier) throws SQLException {
        @Nonnull String query = "SELECT successor FROM general_successor WHERE identifier = " + identifier;
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) return new InternalNonHostIdentifier(resultSet.getString(1));
            else return null;
        }
    }
    
    /**
     * Returns the successor of the given identifier as stored in the database or retrieved by a new request.
     * Please note that the returned successor is only claimed and not yet verified.
     * 
     * @param identifier the identifier whose successor is to be returned.
     * @param verified determines whether the identity of the successor is verified.
     * 
     * @return the successor of the given identifier as stored in the database or retrieved by a new request.
     */
    public static @Nonnull InternalNonHostIdentifier getReloaded(@Nonnull NonHostIdentifier identifier, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        @Nullable InternalNonHostIdentifier successor = get(identifier);
        if (successor == null) {
            final @Nonnull Reply reply;
            if (identifier instanceof InternalNonHostIdentifier) {
                final @Nonnull IdentityReply identityReply = new IdentityQuery((InternalNonHostIdentifier) identifier).sendNotNull();
                successor = identityReply.getSuccessor();
                reply = identityReply;
            } else {
                assert identifier instanceof ExternalIdentifier;
                // TODO: Load the verified successor from 'virtualid.ch' or return null otherwise.
                throw new UnsupportedOperationException("The verification of email addresses is not supported yet.");
            }
            
            if (successor != null) {
                set(identifier, successor, reply);
                Mapper.unmap(identifier.getIdentity());
                if (verified && !successor.getIdentity().equals(identifier.getIdentity()))
                    throw new InvalidDeclarationException("The identifier " + identifier + " and its indicated successor " + successor + " denote different identities.", identifier, reply);
            } else {
                throw new PacketException(PacketError.EXTERNAL, "The identity with the identifier " + identifier + " has not been relocated.");
            }
        }
        return successor;
    }
    
    /**
     * Sets the successor of the given identifier to the given value.
     * 
     * @param identifier the identifier whose successor is to be set.
     * @param successor the successor to be set for the given identifier.
     * @param reply the reply stating that the given identifier has the given successor.
     */
    public static void set(@Nonnull NonHostIdentifier identifier, @Nonnull InternalNonHostIdentifier successor, @Nullable Reply reply) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("INSERT INTO general_successor (identifier, successor, reply) VALUES (" + identifier + ", " + successor + ", " + reply + ")");
        }
    }
    
}
