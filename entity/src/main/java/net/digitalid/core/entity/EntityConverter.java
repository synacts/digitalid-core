package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.unit.CoreUnit;

/**
 * This class implements the converter for {@link Entity}.
 */
@Immutable
public class EntityConverter extends AbstractEntityConverter<Entity> {
    
    public static final @Nonnull EntityConverter INSTANCE = new EntityConverter();
    
    @Pure
    @Override
    public @Nonnull Class<Entity> getType() {
        return Entity.class;
    }
    
    @Pure
    @Override
    public @Nonnull String getTypeName() {
        return "Entity";
    }
    
    @Pure
    @Override
    public @Nonnull String getTypePackage() {
        return "net.digitalid.core.entity";
    }
    
    @Pure
    @Override
    protected @Nonnull Entity recover(@Nonnull CoreUnit unit, long key) throws DatabaseException, RecoveryException {
        return Entity.with(unit, key);
    }
    
}
