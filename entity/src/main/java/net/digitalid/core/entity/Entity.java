package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.subject.Subject;
import net.digitalid.database.unit.Unit;

import net.digitalid.core.entity.annotations.UnitDependency;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.InternalIdentity;

/**
 * An entity captures the {@link Unit unit} and the {@link Identity identity} of a core subject or handler.
 * 
 * @see NonHostEntity
 */
@Immutable
@GenerateConverter
public interface Entity<@Unspecifiable UNIT extends CoreUnit> extends Subject<UNIT>, UnitDependency {
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Returns the number that represents this entity in the database.
     */
    @Pure
    public long getKey();
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    /**
     * Returns the identity of this entity.
     */
    @Pure
    @NonRepresentative
    public @Nonnull InternalIdentity getIdentity();
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    @Pure
    @Override
    public default boolean isOnHost() {
        return getUnit().isHost();
    }
    
    @Pure
    @Override
    public default boolean isOnClient() {
        return getUnit().isClient();
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Recover
    @NonCommitting
    public static @Nonnull Entity<?> with(@Nonnull CoreUnit unit, long key) /* throws DatabaseException */ {
        // TODO: Think about how to recover entities. Maybe make it configurable/injectable?
//        if (unit instanceof Host) {
//            return Account.getNotNull((Host) unit, resultSet, columnIndex);
//        } else if (unit instanceof Client) {
//            return Role.getNotNull((Client) unit, resultSet, columnIndex);
//        } else {
//            throw UnexpectedValueException.with("A unit is either a host or a client.");
//        }
        throw new RuntimeException();
    }
    
    /* -------------------------------------------------- Key Converters -------------------------------------------------- */
    
    // TODO: Remove the following code after having made it work with the generated converter.
    
//    /**
//     * This class allows to convert an entity to its identity and recover it again by downcasting the entity returned by the overridden method to the given target class.
//     */
//    @Immutable
//    public static final class IdentityConverter<E extends Entity> extends CastingNonRequestingKeyConverter<E, Site, InternalIdentity, Object, Entity> {
//        
//        /**
//         * Creates a new entity-identity converter with the given target class.
//         * 
//         * @param targetClass the target class to which the recovered object is cast.
//         */
//        protected IdentityConverter(@Nonnull Class<E> targetClass) {
//            super(targetClass);
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Object decompose(@Nonnull Site site) {
//            return None.OBJECT;
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull InternalIdentity convert(@Nonnull E entity) {
//            return entity.getIdentity();
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Entity recoverSupertype(@Nonnull Site site, @Nonnull InternalIdentity identity) throws InternalException {
//            if (site instanceof Host) {
//                return Account.get((Host) site, identity);
//            } else {
//                // Entities are encoded through their identity, which is not enough to recover roles.
//                // (There can exist several roles for the same identity through different paths.)
//                throw InternalException.get("Roles cannot be recovered from a block.");
//            }
//        }
//        
//    }
//    
//    /**
//     * This class allows to convert an entity to its key and recover it again by downcasting the entity returned by the overridden method to the given target class.
//     */
//    @Immutable
//    public static final class LongConverter<E extends Entity> extends CastingNonRequestingKeyConverter<E, Site, Long, Object, Entity> {
//        
//        /**
//         * Creates a new identity-long converter with the given target class.
//         * 
//         * @param targetClass the target class to which the recovered object is cast.
//         */
//        protected LongConverter(@Nonnull Class<E> targetClass) {
//            super(targetClass);
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Long convert(@Nonnull E entity) {
//            return entity.getKey();
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Entity recoverSupertype(@Nonnull Site site, @Nonnull Long key) throws InvalidEncodingException, InternalException {
//            try {
//                if (site instanceof Host) {
//                    return Account.get((Host) site, Mapper.getIdentity(key).castTo(InternalIdentity.class));
//                } else if (site instanceof Client) {
//                    return Role.get((Client) site, key);
//                } else {
//                    throw InternalException.get("The site is always a host or a client.");
//                }
//            } catch (@Nonnull DatabaseException exception) {
//                throw MaskingInvalidEncodingException.get(exception);
//            }
//        }
//        
//    }
    
    /* -------------------------------------------------- Declaration -------------------------------------------------- */
    
    // TODO: Are we capable to generate different foreign key constraints based on the site?
    
//    /**
//     * The column declaration for identities that registers at the mapper.
//     */
//    @Immutable
//    public static final class Declaration extends ColumnDeclaration {
//        
//        /**
//         * Creates a new entity declaration with the given name.
//         * 
//         * @param name the name of the new entity declaration.
//         */
//        protected Declaration(@Nonnull @Validated String name) {
//            super(name, Integer64Wrapper.SQL_TYPE, null);
//        }
//        
//        @Pure
//        @Override
//        public boolean isSiteSpecific() {
//            return true;
//        }
//        
//        @Locked
//        @Override
//        @NonCommitting
//        protected @Nonnull String getForeignKeys(@Nullable Site site, @Nullable @Validated String prefix) throws SQLException {
//            if (site instanceof Host) {
//                return ", FOREIGN KEY (" + getName(prefix) + ") " + Mapper.REFERENCE.get(null);
//            } else if (site instanceof Client) {
//                return ", FOREIGN KEY (" + getName(prefix) + ") " + Role.REFERENCE.get(site);
//            } else {
//                throw new SQLException("The site is always a host or a client.");
//            }
//        }
//        
//    }
    
}
