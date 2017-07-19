package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.unit.CoreUnit;

/**
 * This class implements the converter for {@link NonHostEntity}.
 */
@Immutable
public class NonHostEntityConverter extends AbstractEntityConverter<NonHostEntity> {
    
    public static final @Nonnull NonHostEntityConverter INSTANCE = new NonHostEntityConverter();
    
    @Pure
    @Override
    public @Nonnull Class<NonHostEntity> getType() {
        return NonHostEntity.class;
    }
    
    @Pure
    @Override
    public @Nonnull String getTypeName() {
        return "NonHostEntity";
    }
    
    @Pure
    @Override
    public @Nonnull String getTypePackage() {
        return "net.digitalid.core.entity";
    }
    
    @Pure
    @Override
    protected @Nonnull NonHostEntity recover(@Nonnull CoreUnit unit, long key) throws DatabaseException, RecoveryException {
        return NonHostEntity.with(unit, key);
    }
    
}
