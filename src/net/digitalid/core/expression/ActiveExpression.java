package net.digitalid.core.expression;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableSet;
import net.digitalid.core.contact.Contact;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.StringWrapper;

/**
 * This class models active expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class ActiveExpression extends AbstractExpression {
    
    /**
     * Stores the semantic type {@code active.expression@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("active.expression@core.digitalid.net").load(StringWrapper.TYPE);
    
    
    /**
     * Creates a new active expression with the given entity and string.
     * 
     * @param entity the entity to which this active expression belongs.
     * @param string the string which is to be parsed for the expression.
     */
    @NonCommitting
    public ActiveExpression(@Nonnull NonHostEntity entity, @Nonnull String string) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, string);
    }
    
    /**
     * Creates a new active expression from the given entity and block.
     * 
     * @param entity the entity to which this active expression belongs.
     * @param block the block which contains the active expression.
     * 
     * @require block.getType().isBasedOn(StringWrapper.TYPE) : "The block is based on the string type.";
     */
    @NonCommitting
    public ActiveExpression(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    boolean isValid() {
        return getExpression().isActive();
    }
    
    
    /**
     * Returns the contacts denoted by this active expression.
     * 
     * @return the contacts denoted by this active expression.
     */
    @Pure
    @NonCommitting
    public @Nonnull @Capturable FreezableSet<Contact> getContacts() throws SQLException {
        return getExpression().getContacts();
    }
    
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param entity the entity to which the returned expression belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull ActiveExpression get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            return new ActiveExpression(entity, resultSet.getString(columnIndex));
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("The expression returned by the database is invalid.", exception);
        }
    }
    
}
