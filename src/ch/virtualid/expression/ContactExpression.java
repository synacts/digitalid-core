package ch.virtualid.expression;

import ch.virtualid.credential.Credential;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.xdf.Block;
import java.sql.SQLException;

/**
 * This class models contact expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.8
 */
final class ContactExpression extends Expression {

    /**
     * Stores the contact of this expression.
     */
    private final long contact;

    /**
     * Creates a new contact expression with the given contact.
     * 
     * @param connection a connection to the database.
     * @param host the host of the VID.
     * @param vid the VID of the contexts.
     * @param contact the contact to use.
     * @require Mapper.isVid(contact) && Category.isSemanticType(contact) : "The second number has to denote a person.";
     */
    ContactExpression(long contact) throws SQLException {
        super(null, null, 0);

        assert Mapper.isVid(contact) && Category.isPerson(contact) : "The second number has to denote a person.";

        this.contact = contact;
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

        for (Credential credential : credentials) {
            if (credential.getAttribute() == null && Mapper.getVid(credential.getIdentifier()) == contact) return true;
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
        try { return Mapper.getIdentifier(contact); } catch (SQLException exception) { return "ERROR"; }
    }
}
