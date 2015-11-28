package net.digitalid.service.core.concept;

import javax.annotation.Nonnull;
import net.digitalid.database.core.site.Site;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.factory.ConceptConverters;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.storage.Service;
import net.digitalid.service.core.storage.SiteModule;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class stores the setup of a {@link Concept concept}.
 */
@Immutable
public final class ConceptSetup<C extends Concept<C, E, K>, E extends Entity, K> {
    
    /* -------------------------------------------------- Concept Service -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Concept Name -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Concept ConceptIndex -------------------------------------------------- */
    
    /**
     * Stores the index used to cache instances of the concept.
     */
    private final @Nonnull ConceptIndex<C, E, K> conceptIndex;
    
    /**
     * Returns the index used to cache instances of the concept.
     * 
     * @return the index used to cache instances of the concept.
     */
    @Pure
    public @Nonnull ConceptIndex<C, E, K> getConceptIndex() {
        return conceptIndex;
    }
    
    /* -------------------------------------------------- Key Converters -------------------------------------------------- */
    
    /**
     * Stores the converters to convert and recover the key.
     */
    private final @Nonnull Converters<K, E> keyConverters;
    
    /**
     * Returns the converters to convert and recover the key.
     * 
     * @return the converters to convert and recover the key.
     */
    @Pure
    public @Nonnull Converters<K, E> getKeyConverters() {
        return keyConverters;
    }
    
    /* -------------------------------------------------- Entity Converters -------------------------------------------------- */
    
    /**
     * Stores the converters to convert and recover the entity.
     */
    private final @Nonnull Converters<E, Site> entityConverters;
    
    /**
     * Returns the converters to convert and recover the entity.
     * 
     * @return the converters to convert and recover the entity.
     */
    @Pure
    public @Nonnull Converters<E, Site> getEntityConverters() {
        return entityConverters;
    }
    
    /* -------------------------------------------------- State Module -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Concept Type -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the factory used to encode and decode the concept.
     */
    private final @Nonnull ConceptXDFConverter<C, E, K> XDFConverter;
    
    /**
     * Returns the factory used to encode and decode the concept.
     * 
     * @return the factory used to encode and decode the concept.
     */
    @Pure
    public @Nonnull ConceptXDFConverter<C, E, K> getXDFConverter() {
        return XDFConverter;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the factory used to store and restore the concept.
     */
    private final @Nonnull ConceptSQLConverter<C, E, K> SQLConverter;
    
    /**
     * Returns the factory used to store and restore the concept.
     * 
     * @return the factory used to store and restore the concept.
     */
    @Pure
    public @Nonnull ConceptSQLConverter<C, E, K> getSQLConverter() {
        return SQLConverter;
    }
    
    /* -------------------------------------------------- Concept Converters -------------------------------------------------- */
    
    /**
     * Stores the converters to convert and recover the concept.
     */
    private final @Nonnull ConceptConverters<C, E> conceptConverters;
    
    /**
     * Returns the converters to convert and recover the concept.
     * 
     * @return the converters to convert and recover the concept.
     */
    @Pure
    public @Nonnull ConceptConverters<C, E> getConceptConverters() {
        return conceptConverters;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new concept setup with the given parameters.
     * 
     * @param service the service to which the concept belongs.
     * @param conceptName the name of the concept (unique within the service).
     * @param conceptIndex the index used to cache instances of the concept.
     * @param keyConverters the converters to convert and recover the key.
     * @param entityConverters the converters to convert and recover the entity.
     */
    private ConceptSetup(@Nonnull Service service, @Nonnull @Validated String conceptName, @Nonnull ConceptIndex<C, E, K> conceptIndex, @Nonnull Converters<K, E> keyConverters, @Nonnull Converters<E, Site> entityConverters) {
        this.service = service;
        this.conceptName = conceptName;
        this.conceptIndex = conceptIndex;
        this.keyConverters = keyConverters;
        this.entityConverters = entityConverters;
        
        this.siteModule = SiteModule.get(service, conceptName);
        this.conceptType = SemanticType.map(conceptName + service.getType().getAddress().getStringWithDot()).load(keyConverters.getXDFConverter().getType());
        
        this.XDFConverter = ConceptXDFConverter.get(keyConverters.getXDFConverter().setType(conceptType), conceptIndex);
        this.SQLConverter = ConceptSQLConverter.get(keyConverters.getSQLConverter(), conceptIndex);
        this.conceptConverters = ConceptConverters.get(XDFConverter, SQLConverter);
    }
    
    /**
     * Creates a new concept setup with the given parameters.
     * 
     * @param service the service to which the concept belongs.
     * @param conceptName the name of the concept (unique within the service).
     * @param conceptIndex the index used to cache instances of the concept.
     * @param keyConverters the converters to convert and recover the key.
     * @param entityConverters the converters to convert and recover the entity.
     * 
     * @return a new concept setup with the given parameters.
     */
    @Pure
    public static @Nonnull <C extends Concept<C, E, K>, E extends Entity, K> ConceptSetup<C, E, K> get(@Nonnull Service service, @Nonnull @Validated String conceptName, @Nonnull ConceptIndex<C, E, K> conceptIndex, @Nonnull Converters<K, E> keyConverters, @Nonnull Converters<E, Site> entityConverters) {
        return new ConceptSetup<>(service, conceptName, conceptIndex, keyConverters, entityConverters);
    }
    
}
