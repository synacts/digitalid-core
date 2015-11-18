package net.digitalid.service.core.entity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.block.wrappers.Int64Wrapper;
import net.digitalid.service.core.concept.Concept;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.key.Caster;
import net.digitalid.service.core.converter.key.CastingNonRequestingKeyConverter;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.handler.Handler;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.SQL;
import net.digitalid.utility.database.declaration.ColumnDeclaration;
import net.digitalid.utility.database.site.Site;

/**
 * An entity captures the {@link Site site} and the {@link Identity identity} of a {@link Concept concept} or {@link Handler handler}.
 * 
 * @see EntityImplementation
 * @see HostEntity
 * @see NonHostEntity
 */
@Immutable
public interface Entity extends XDF<Entity, Site>, SQL<Entity, Site> {
    
    /* -------------------------------------------------- Methods -------------------------------------------------- */
    
    /**
     * Returns the site of this entity.
     * 
     * @return the site of this entity.
     */
    @Pure
    public @Nonnull Site getSite();
    
    /**
     * Returns the identity of this entity.
     * 
     * @return the identity of this entity.
     */
    @Pure
    public @Nonnull InternalIdentity getIdentity();
    
    /**
     * Returns the number that references this entity in the database.
     * 
     * @return the number that references this entity in the database.
     */
    @Pure
    public long getKey();
    
    /* -------------------------------------------------- Casting -------------------------------------------------- */
    
    /**
     * Returns this entity as a {@link HostEntity}.
     * 
     * @return this entity as a {@link HostEntity}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link HostEntity}.
     */
    @Pure
    public @Nonnull HostEntity toHostEntity() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link NonHostEntity}.
     * 
     * @return this entity as a {@link NonHostEntity}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link NonHostEntity}.
     */
    @Pure
    public @Nonnull NonHostEntity toNonHostEntity() throws InvalidEncodingException;
    
    /**
     * Returns this entity as an {@link Account}.
     * 
     * @return this entity as an {@link Account}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link Account}.
     */
    @Pure
    public @Nonnull Account toAccount() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link HostAccount}.
     * 
     * @return this entity as a {@link HostAccount}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link HostAccount}.
     */
    @Pure
    public @Nonnull HostAccount toHostAccount() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link NonHostAccount}.
     * 
     * @return this entity as a {@link NonHostAccount}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link NonHostAccount}.
     */
    @Pure
    public @Nonnull NonHostAccount toNonHostAccount() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link Role}.
     * 
     * @return this entity as a {@link Role}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link Role}.
     */
    @Pure
    public @Nonnull Role toRole() throws InvalidEncodingException;
    
    /* -------------------------------------------------- Key Converters -------------------------------------------------- */
    
    /**
     * This class allows to convert an entity to its identity and recover it again by downcasting the entity returned by the overridden method with the given caster.
     */
    @Immutable
    public static final class IdentityConverter<E extends Entity> extends CastingNonRequestingKeyConverter<E, Site, InternalIdentity, Object, Entity> {
        
        /**
         * Creates a new entity-identity converter with the given caster.
         * 
         * @param caster the caster that allows to cast objects to the specified subtype.
         */
        protected IdentityConverter(@Nonnull Caster<Entity, E> caster) {
            super(caster);
        }
        
        @Pure
        @Override
        public @Nonnull Object decompose(@Nonnull Site site) {
            return None.OBJECT;
        }
        
        @Pure
        @Override
        public @Nonnull InternalIdentity convert(@Nonnull E entity) {
            return entity.getIdentity();
        }
        
        @Pure
        @Override
        public @Nonnull Entity recoverSupertype(@Nonnull Site site, @Nonnull InternalIdentity identity) throws InvalidEncodingException {
            if (site instanceof Host) {
                return Account.get((Host) site, identity);
            } else {
                // Entities are encoded through their identity, which is not enough to recover roles.
                // (There can exist several roles for the same identity through different paths.)
                throw new InvalidEncodingException("Roles cannot be recovered from a block.");
            }
        }
        
    }
    
    /**
     * This class allows to convert an entity to its key and recover it again by downcasting the entity returned by the overridden method with the given caster.
     */
    @Immutable
    public static final class LongConverter<E extends Entity> extends CastingNonRequestingKeyConverter<E, Site, Long, Object, Entity> {
        
        /**
         * Creates a new identity-long converter with the given caster.
         * 
         * @param caster the caster that allows to cast objects to the specified subtype.
         */
        protected LongConverter(@Nonnull Caster<Entity, E> caster) {
            super(caster);
        }
        
        @Pure
        @Override
        public @Nonnull Long convert(@Nonnull E entity) {
            return entity.getKey();
        }
        
        @Pure
        @Override
        public @Nonnull Entity recoverSupertype(@Nonnull Site site, @Nonnull Long key) throws InvalidEncodingException {
            try {
                if (site instanceof Host) {
                    return Account.get((Host) site, Mapper.getIdentity(key).toInternalIdentity());
                } else if (site instanceof Client) {
                    return Role.get((Client) site, key);
                } else {
                    throw new InvalidEncodingException("The site is always a host or a client.");
                }
            } catch (@Nonnull AbortException exception) {
                throw new InvalidEncodingException(exception);
            }
        }
        
    }
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    public static final @Nonnull Caster<Entity, Entity> CASTER = new Caster<Entity, Entity>() {
        @Pure
        @Override
        protected @Nonnull Entity cast(@Nonnull Entity entity) throws InvalidEncodingException {
            return entity;
        }
    };
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractXDFConverter<Entity, Site> XDF_CONVERTER = ChainingXDFConverter.get(new Entity.IdentityConverter<>(CASTER), InternalIdentity.XDF_CONVERTER);
    
    /* -------------------------------------------------- Declaration -------------------------------------------------- */
    
    /**
     * The column declaration for identities that registers at the mapper.
     */
    @Immutable
    public static final class Declaration extends ColumnDeclaration {
        
        /**
         * Creates a new entity declaration with the given name.
         * 
         * @param name the name of the new entity declaration.
         */
        protected Declaration(@Nonnull @Validated String name) {
            super(name, Int64Wrapper.SQL_TYPE, null);
        }
        
        @Pure
        @Override
        public boolean isSiteSpecific() {
            return true;
        }
        
        @Locked
        @Override
        @NonCommitting
        protected @Nonnull String getForeignKeys(@Nullable Site site, @Nullable @Validated String prefix) throws SQLException {
            if (site instanceof Host) {
                return ", FOREIGN KEY (" + getName(prefix) + ") " + Mapper.REFERENCE.get(null);
            } else if (site instanceof Client) {
                return ", FOREIGN KEY (" + getName(prefix) + ") " + Role.REFERENCE.get(site);
            } else {
                throw new SQLException("The site is always a host or a client.");
            }
        }
        
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Entity.Declaration DECLARATION = new Entity.Declaration("entity");
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<Entity, Site> SQL_CONVERTER = ChainingSQLConverter.get(new Entity.LongConverter<>(CASTER), Int64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Entity, Site> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
