package net.digitalid.core.expression;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.packet.exceptions.RequestException;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.string.StringWrapper;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.identity.SemanticType;

/**
 * This class models active expressions.
 */
@Immutable
public final class ActiveExpression extends AbstractExpression {
    
    /**
     * Stores the semantic type {@code active.expression@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("active.expression@core.digitalid.net").load(StringWrapper.XDF_TYPE);
    
    
    /**
     * Creates a new active expression with the given entity and string.
     * 
     * @param entity the entity to which this active expression belongs.
     * @param string the string which is to be parsed for the expression.
     */
    @NonCommitting
    public ActiveExpression(@Nonnull NonHostEntity entity, @Nonnull String string) throws ExternalException {
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
    public ActiveExpression(@Nonnull NonHostEntity entity, @Nonnull Block block) throws ExternalException {
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
    public @Nonnull @Capturable FreezableSet<Contact> getContacts() throws DatabaseException {
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
    public static @Nonnull ActiveExpression get(@Nonnull NonHostEntity entity, @NonCapturable @Nonnull SelectionResult result) throws DatabaseException {
        try {
            return new ActiveExpression(entity, resultSet.getString(columnIndex));
        } catch (@Nonnull IOException | RequestException | ExternalException exception) {
            throw new SQLException("The expression returned by the database is invalid.", exception);
        }
    }
    
}
