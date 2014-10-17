package ch.virtualid.expression;

import ch.virtualid.credential.Credential;
import ch.virtualid.server.Host;
import ch.xdf.Block;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class models context expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.8
 */
final class ContextExpression extends Expression {

    /**
     * Stores the context of this expression.
     */
    private final int context;

    /**
     * Creates a new context expression with the given context.
     * 
     * @param connection a connection to the database.
     * @param host the host of the VID.
     * @param vid the VID of the contexts.
     * @param context the context to use.
     * @require connection != null : "The connection is not null.";
     * @require host != null : "The host is not null.";
     * @require Mapper.isVid(vid) : "The first number has to denote a VID.";
     */
    ContextExpression(Connection connection, Host host, long vid, int context) throws SQLException {
        super(connection, host, vid);

        assert connection != null : "The connection is not null.";
        assert host != null : "The host is not null.";
//        assert Mapper.isVid(vid) : "The first number has to denote a VID.";

        this.context = context;
    }

    /**
     * Returns whether this expression is active.
     * 
     * @return whether this expression is active.
     */
    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * Returns whether this expression matches the given block (for certification restrictions).
     * 
     * @param attribute the attribute to check.
     * @return whether this expression matches the given block.
     * @require false : "This method should never be called.";
     */
    @Override
    public boolean matches(Block attribute) {
        assert false : "This method should never be called.";

        return false;
    }

    /**
     * Returns whether this expression matches the given credentials.
     * 
     * @param credentials the credentials to check.
     * @return whether this expression matches the given credentials.
     */
    @Override
    public boolean matches(Credential[] credentials) throws SQLException, Exception {
        if (credentials == null) return false;

//        List<Long> contacts = getHost().getContacts(getConnection(), getVid(), context);

        for (Credential credential : credentials) {
//            if (credential.getAttribute() == null && contacts.contains(Mapper.getVid(credential.getIdentifier()))) return true;
        }

        return false;
    }

    /**
     * Returns this expression as a string.
     * 
     * @return this expression as a string.
     */
    @Override
    public String toString() {
        return Integer.toString(context);
    }
}
