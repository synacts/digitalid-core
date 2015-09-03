package net.digitalid.core.storable;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Captured;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.database.Column;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * This class is like {@link SimpleFactory} except that no additional information is needed to reconstruct the object.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class SelfcontainedSimpleFactory<O> extends Factory<O, Void> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public abstract @Nonnull O decodeNonNullable(@Nullable Void entity, @Nonnull @NonEncoding Block block) throws InvalidEncodingException;
    
    @Pure
    @Override
    public @Nullable O decodeNullable(@Nullable Void entity, @Nullable @NonEncoding Block block) throws InvalidEncodingException {
        if (block != null) return decodeNonNullable(entity, block);
        else return null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieving –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nullable O getNullable(@Nullable Void entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException;
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull O getNonNullable(@Nullable Void entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nullable O object = getNullable(entity, resultSet, columnIndex);
        if (object == null) throw new SQLException("An object which should not be null was null.");
        return object;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new selfcontained simple factory with the given parameters.
     * 
     * @param type the semantic type that corresponds to the storable class.
     * @param columns the columns used to store objects of the storable class.
     */
    protected SelfcontainedSimpleFactory(@Nonnull @Loaded SemanticType type, @Captured @Nonnull @NonNullableElements Column... columns) {
        super(type, columns);
    }
    
}
