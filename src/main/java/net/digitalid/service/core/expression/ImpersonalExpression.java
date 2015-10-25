package net.digitalid.service.core.expression;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.StringWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models impersonal expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class ImpersonalExpression extends AbstractExpression {
    
    /**
     * Stores the semantic type {@code impersonal.expression@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("impersonal.expression@core.digitalid.net").load(StringWrapper.TYPE);
    
    
    /**
     * Creates a new impersonal expression with the given entity and string.
     * 
     * @param entity the entity to which this impersonal expression belongs.
     * @param string the string which is to be parsed for the expression.
     */
    @NonCommitting
    public ImpersonalExpression(@Nonnull NonHostEntity entity, @Nonnull String string) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, string);
    }
    
    /**
     * Creates a new impersonal expression from the given entity and block.
     * 
     * @param entity the entity to which this impersonal expression belongs.
     * @param block the block which contains the impersonal expression.
     * 
     * @require block.getType().isBasedOn(StringWrapper.TYPE) : "The block is based on the string type.";
     */
    @NonCommitting
    public ImpersonalExpression(@Nonnull NonHostEntity entity, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
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
        return getExpression().isImpersonal();
    }
    
    
    /**
     * Returns whether this impersonal expression matches the given attribute content.
     * 
     * @param attributeContent the attribute content which is to be checked.
     * 
     * @return whether this impersonal expression matches the given attribute content.
     */
    @Pure
    public boolean matches(@Nonnull Block attributeContent) {
        return getExpression().matches(attributeContent);
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
    public static @Nonnull ImpersonalExpression get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            return new ImpersonalExpression(entity, resultSet.getString(columnIndex));
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("The expression returned by the database is invalid.", exception);
        }
    }
    
}
