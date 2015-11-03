package net.digitalid.service.core.concept;

import javax.annotation.Nonnull;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.factory.ConceptFactories;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.storage.Service;
import net.digitalid.service.core.storage.SiteModule;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.site.Site;

/**
 * This class stores the setup of a {@link Concept concept}.
 */
@Immutable
public final class ConceptSetup<C extends Concept<C, E, K>, E extends Entity<E>, K> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept Service –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the service to which the concept belongs.
     */
    private final @Nonnull Service service;
    
    /**
     * Returns the service to which the concept belongs.
     * 
     * @return the service to which the concept belongs.
     */
    @Pure
    public @Nonnull Service getService() {
        return service;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept Name –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the name of the concept (unique within the service).
     */
    private final @Nonnull @Validated String conceptName;
    
    /**
     * Returns the name of the concept (unique within the service).
     * 
     * @return the name of the concept (unique within the service).
     */
    @Pure
    public @Nonnull @Validated String getConceptName() {
        return conceptName;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept Index –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the index used to cache instances of the concept.
     */
    private final @Nonnull Index<C, E, K> conceptIndex;
    
    /**
     * Returns the index used to cache instances of the concept.
     * 
     * @return the index used to cache instances of the concept.
     */
    @Pure
    public @Nonnull Index<C, E, K> getConceptIndex() {
        return conceptIndex;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Key Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories to convert and reconstruct the key.
     */
    private final @Nonnull Factories<K, E> keyFactories;
    
    /**
     * Returns the factories to convert and reconstruct the key.
     * 
     * @return the factories to convert and reconstruct the key.
     */
    @Pure
    public @Nonnull Factories<K, E> getKeyFactories() {
        return keyFactories;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Entity Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories to convert and reconstruct the entity.
     */
    private final @Nonnull Factories<E, Site> entityFactories;
    
    /**
     * Returns the factories to convert and reconstruct the entity.
     * 
     * @return the factories to convert and reconstruct the entity.
     */
    @Pure
    public @Nonnull Factories<E, Site> getEntityFactories() {
        return entityFactories;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– State Module –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the module to which the property table belongs.
     */
    private final @Nonnull SiteModule siteModule;
    
    /**
     * Returns the module to which the property table belongs.
     * 
     * @return the module to which the property table belongs.
     */
    @Pure
    public @Nonnull SiteModule getSiteModule() {
        return siteModule;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type to encode the concept.
     */
    private final @Nonnull SemanticType conceptType;
    
    /**
     * Returns the semantic type to encode the concept.
     * 
     * @return the semantic type to encode the concept.
     */
    @Pure
    public @Nonnull SemanticType getConceptType() {
        return conceptType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory used to encode and decode the concept.
     */
    private final @Nonnull ConceptEncodingFactory<C, E, K> encodingFactory;
    
    /**
     * Returns the factory used to encode and decode the concept.
     * 
     * @return the factory used to encode and decode the concept.
     */
    @Pure
    public @Nonnull ConceptEncodingFactory<C, E, K> getEncodingFactory() {
        return encodingFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory used to store and restore the concept.
     */
    private final @Nonnull ConceptStoringFactory<C, E, K> storingFactory;
    
    /**
     * Returns the factory used to store and restore the concept.
     * 
     * @return the factory used to store and restore the concept.
     */
    @Pure
    public @Nonnull ConceptStoringFactory<C, E, K> getStoringFactory() {
        return storingFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Concept Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories to convert and reconstruct the concept.
     */
    private final @Nonnull ConceptFactories<C, E> conceptFactories;
    
    /**
     * Returns the factories to convert and reconstruct the concept.
     * 
     * @return the factories to convert and reconstruct the concept.
     */
    @Pure
    public @Nonnull ConceptFactories<C, E> getConceptFactories() {
        return conceptFactories;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new concept setup with the given parameters.
     * 
     * @param service the service to which the concept belongs.
     * @param conceptName the name of the concept (unique within the service).
     * @param conceptIndex the index used to cache instances of the concept.
     * @param keyFactories the factories to convert and reconstruct the key.
     * @param entityFactories the factories to convert and reconstruct the entity.
     */
    private ConceptSetup(@Nonnull Service service, @Nonnull @Validated String conceptName, @Nonnull Index<C, E, K> conceptIndex, @Nonnull Factories<K, E> keyFactories, @Nonnull Factories<E, Site> entityFactories) {
        this.service = service;
        this.conceptName = conceptName;
        this.conceptIndex = conceptIndex;
        this.keyFactories = keyFactories;
        this.entityFactories = entityFactories;
        
        this.siteModule = SiteModule.get(service, conceptName);
        this.conceptType = SemanticType.map(conceptName + service.getType().getAddress().getStringWithDot()).load(keyFactories.getEncodingFactory().getType());
        
        this.encodingFactory = ConceptEncodingFactory.get(keyFactories.getEncodingFactory().setType(conceptType), conceptIndex);
        this.storingFactory = ConceptStoringFactory.get(keyFactories.getStoringFactory(), conceptIndex);
        this.conceptFactories = ConceptFactories.get(encodingFactory, storingFactory);
    }
    
    /**
     * Creates a new concept setup with the given parameters.
     * 
     * @param service the service to which the concept belongs.
     * @param conceptName the name of the concept (unique within the service).
     * @param conceptIndex the index used to cache instances of the concept.
     * @param keyFactories the factories to convert and reconstruct the key.
     * @param entityFactories the factories to convert and reconstruct the entity.
     * 
     * @return a new concept setup with the given parameters.
     */
    @Pure
    public static @Nonnull <C extends Concept<C, E, K>, E extends Entity<E>, K> ConceptSetup<C, E, K> get(@Nonnull Service service, @Nonnull @Validated String conceptName, @Nonnull Index<C, E, K> conceptIndex, @Nonnull Factories<K, E> keyFactories, @Nonnull Factories<E, Site> entityFactories) {
        return new ConceptSetup<>(service, conceptName, conceptIndex, keyFactories, entityFactories);
    }
    
}
