/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.cache;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.ConversionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.dialect.expression.bool.SQLBooleanExpression;
import net.digitalid.database.dialect.expression.number.SQLLongLiteralBuilder;
import net.digitalid.database.dialect.identifier.column.SQLColumnName;
import net.digitalid.database.dialect.identifier.column.SQLColumnNameBuilder;
import net.digitalid.database.dialect.identifier.schema.SQLSchemaName;
import net.digitalid.database.dialect.identifier.schema.SQLSchemaNameBuilder;
import net.digitalid.database.dialect.identifier.table.SQLExplicitlyQualifiedTableBuilder;
import net.digitalid.database.dialect.identifier.table.SQLQualifiedTable;
import net.digitalid.database.dialect.identifier.table.SQLTableName;
import net.digitalid.database.dialect.identifier.table.SQLTableNameBuilder;
import net.digitalid.database.dialect.statement.select.unordered.simple.SQLSimpleSelectStatement;
import net.digitalid.database.dialect.statement.select.unordered.simple.SQLSimpleSelectStatementBuilder;
import net.digitalid.database.dialect.statement.select.unordered.simple.columns.SQLResultColumn;
import net.digitalid.database.dialect.statement.select.unordered.simple.columns.SQLResultColumnBuilder;
import net.digitalid.database.dialect.statement.select.unordered.simple.sources.SQLTableSource;
import net.digitalid.database.dialect.statement.select.unordered.simple.sources.SQLTableSourceBuilder;
import net.digitalid.database.dialect.statement.update.SQLAssignment;
import net.digitalid.database.dialect.statement.update.SQLAssignmentBuilder;
import net.digitalid.database.dialect.statement.update.SQLUpdateStatement;
import net.digitalid.database.dialect.statement.update.SQLUpdateStatementBuilder;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.interfaces.SQLDecoder;
import net.digitalid.database.interfaces.encoder.SQLActionEncoder;
import net.digitalid.database.interfaces.encoder.SQLQueryEncoder;

import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.attribute.AttributePropertiesLoader;
import net.digitalid.core.cache.errors.MissingTrustAnchorErrorBuilder;
import net.digitalid.core.client.ClientSecretLoader;
import net.digitalid.core.client.role.Role;
import net.digitalid.core.client.role.RoleModule;
import net.digitalid.core.entity.factories.AccountFactory;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.keychain.PublicKeyChain;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.PackConverter;
import net.digitalid.core.signature.attribute.AttributeValue;
import net.digitalid.core.signature.attribute.AttributeValueConverter;
import net.digitalid.core.signature.attribute.CertifiedAttributeValueConverter;
import net.digitalid.core.unit.GeneralUnit;

/**
 * This class provides database access to the {@link Cache cache}.
 */
@Utility
public abstract class CacheModule {
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores a dummy configuration in order to have an initialization target for table creation.
     */
    public static final @Nonnull Configuration<Boolean> configuration = Configuration.with(Boolean.TRUE);
    
    /* -------------------------------------------------- Creation -------------------------------------------------- */
    
    /**
     * Creates the database table.
     */
    @Committing
    @PureWithSideEffects
    @Initialize(target = CacheModule.class, dependencies = RoleModule.class)
    public static void createTable() throws DatabaseException {
        SQL.createTable(CacheEntryConverter.INSTANCE, GeneralUnit.INSTANCE);
    }
    
    /* -------------------------------------------------- Root Key -------------------------------------------------- */
    
    /**
     * Initializes the cache with the public key of {@code core.digitalid.net}.
     */
    @Committing
    @PureWithSideEffects
    @TODO(task = "Provide the correct parameters for the loading of the type.", date = "2017-10-05", author = Author.KASPAR_ETTER)
    @Initialize(target = CacheModule.class, dependencies = {AccountFactory.class, AttributePropertiesLoader.class, PrivateKeyRetriever.class, ClientSecretLoader.class})
    public static void initializeRootKey() throws ConversionException {
        if (!getCachedAttributeValue(null, HostIdentity.DIGITALID, Time.MIN, PublicKeyChain.TYPE).get0()) {
            // Unless it is the root server, the program should have been delivered with the public key chain of 'core.digitalid.net'.
            final @Nullable InputStream inputStream = Cache.class.getResourceAsStream("/net/digitalid/core/cache/core.digitalid.net.certificate.xdf");
            final @Nonnull AttributeValue value;
            if (inputStream != null) {
                final @Nonnull SemanticType semanticType = SemanticType.map(CertifiedAttributeValueConverter.INSTANCE);
                semanticType.load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build());
                Database.commit(); // The type mapping has to be committed because the local map is not updated otherwise.
                value = Pack.loadFrom(inputStream).unpack(CertifiedAttributeValueConverter.INSTANCE, null);
                Log.debugging("The public key chain of the root host $ was loaded from the provided resources.", HostIdentifier.DIGITALID);
            } else {
                throw MissingTrustAnchorErrorBuilder.withMessage("The library cannot be used without a trust anchor.").build();
            }
            setCachedAttributeValue(null, HostIdentity.DIGITALID, Time.MIN, PublicKeyChain.TYPE, value, null);
        }
        Database.commit();
    }
    
    /* -------------------------------------------------- Dialect Constants -------------------------------------------------- */
    
    private static final @Nonnull SQLTableName tableName = SQLTableNameBuilder.withString(CacheEntryConverter.INSTANCE.getTypeName()).build();
    
    private static final @Nonnull SQLSchemaName schemaName = SQLSchemaNameBuilder.withString(GeneralUnit.INSTANCE.getName()).build();
    
    private static final @Nonnull SQLQualifiedTable qualifiedTable = SQLExplicitlyQualifiedTableBuilder.withTable(tableName).withSchema(schemaName).build();
    
    /* -------------------------------------------------- Cache Invalidation -------------------------------------------------- */
    
    /**
     * Invalidates all the cached attribute values of the given identity.
     */
    @NonCommitting
    @PureWithSideEffects
    public static void invalidateCachedAttributeValues(@Nonnull InternalNonHostIdentity identity) throws DatabaseException {
        final @Nonnull Time currentTime = TimeBuilder.build();
        final @Nonnull SQLAssignment assignment = SQLAssignmentBuilder.withColumn(SQLColumnNameBuilder.withString("expirationtime_value").build()).withExpression(SQLLongLiteralBuilder.withValue(currentTime.getValue()).build()).build();
        @Nonnull SQLBooleanExpression whereClause = SQLColumnNameBuilder.withString("requestee_key").build().equal(SQLLongLiteralBuilder.withValue(identity.getKey()).build()); // TODO: Implement it in such a way that the representation of the internal identity can change.
        whereClause = whereClause.and(SQLColumnNameBuilder.withString("expirationtime_value").build().greater(SQLLongLiteralBuilder.withValue(currentTime.getValue()).build())); // TODO: Implement it in such a way that the representation of the time can change?
        
        final @Nonnull SQLUpdateStatement updateStatement = SQLUpdateStatementBuilder.withTable(qualifiedTable).withAssignments(ImmutableList.withElements(assignment)).withWhereClause(whereClause).build();
        final @Nonnull SQLActionEncoder actionEncoder = Database.instance.get().getEncoder(updateStatement, GeneralUnit.INSTANCE);
        actionEncoder.execute();
    }
    
    /* -------------------------------------------------- Cache Reading -------------------------------------------------- */
    
    /**
     * Returns the cached attribute value with the given type of the given requestee as queried by the given requester.
     * 
     * @param requester the role that queries the attribute value or null for hosts (respectively an anonymous query for public attributes).
     * @param requestee the identity whose cached attribute value is to be returned.
     * @param expiration the time at which the cached attribute value has to be fresh.
     * @param type the type of the attribute value which is to be returned.
     * 
     * @return a pair of a boolean indicating whether the attribute value of the given type is cached and the value being cached or null if it is not available (which allows to cache non-availability).
     * 
     * @require type.isAttributeFor(requestee.getCategory()) : "The type can be used as an attribute for the category of the given requestee.";
     * 
     * @ensure return.getValue1() == null || return.getValue1().getContent().getType().equals(type) : "The content of the returned attribute value is null or matches the given type.";
     */
    @Pure
    @NonCommitting
    static @Nonnull Pair<@Nonnull Boolean, @Nullable AttributeValue> getCachedAttributeValue(@Nullable Role requester, @Nonnull InternalIdentity requestee, @Nonnull @NonNegative Time expiration, @Nonnull @AttributeType SemanticType type) throws DatabaseException, RecoveryException {
        Require.that(expiration.isNonNegative()).orThrow("The given time has to be non-negative but was $.", expiration);
        Require.that(type.isAttributeFor(requestee.getCategory())).orThrow("The type $ cannot be used as an attribute for the category $ of the given identity $.", type, requestee.getCategory(), requestee);
        
        if (expiration.equals(Time.MAX)) { return Pair.of(false, null); }
        
        final @Nonnull SQLResultColumn foundColumn = SQLResultColumnBuilder.withExpression(SQLColumnNameBuilder.withString("found").build()).build();
        final @Nonnull SQLResultColumn attributeValueColumn1 = SQLResultColumnBuilder.withExpression(SQLColumnNameBuilder.withString("attributevalue_type_key").build()).build();
        final @Nonnull SQLResultColumn attributeValueColumn2 = SQLResultColumnBuilder.withExpression(SQLColumnNameBuilder.withString("attributevalue_bytes").build()).build();
        final @Nonnull ImmutableList<@Nonnull SQLResultColumn> resultColumns = ImmutableList.withElements(foundColumn, attributeValueColumn1, attributeValueColumn2);
        
        final @Nonnull ImmutableList<@Nonnull SQLTableSource> sources = ImmutableList.withElements(SQLTableSourceBuilder.withSource(qualifiedTable).build());
        
        final @Nonnull SQLColumnName requesterColumnName = SQLColumnNameBuilder.withString("requester").build();
        @Nonnull SQLBooleanExpression whereClause = requesterColumnName.equal(SQLLongLiteralBuilder.withValue(0).build());
        if (requester != null) { whereClause = whereClause.or(requesterColumnName.equal(SQLLongLiteralBuilder.withValue(requester.getKey()).build())); }
        
        whereClause = whereClause.and(SQLColumnNameBuilder.withString("requestee_key").build().equal(SQLLongLiteralBuilder.withValue(requestee.getKey()).build())); // TODO: Implement it in such a way that the representation of the internal identity can change.
        whereClause = whereClause.and(SQLColumnNameBuilder.withString("attributetype_key").build().equal(SQLLongLiteralBuilder.withValue(type.getKey()).build())); // TODO: Implement it in such a way that the representation of the semantic type can change.
        whereClause = whereClause.and(SQLColumnNameBuilder.withString("expirationtime_value").build().greaterOrEqual(SQLLongLiteralBuilder.withValue(expiration.getValue()).build())); // TODO: Implement it in such a way that the representation of the time can change?
        
        final @Nonnull SQLSimpleSelectStatement selectStatement = SQLSimpleSelectStatementBuilder.withColumns(resultColumns).withSources(sources).withWhereClause(whereClause).build();
        
        final @Nonnull SQLQueryEncoder queryEncoder = Database.instance.get().getEncoder(selectStatement, GeneralUnit.INSTANCE);
        final @Nonnull SQLDecoder decoder = queryEncoder.execute();
        
        boolean found = false;
        @Nullable AttributeValue value = null;
        if (decoder.moveToNextRow()) {
            found = true;
            do {
                if (decoder.decodeBoolean()) {
                    final @Nonnull Pack pack = PackConverter.INSTANCE.recover(decoder, null);
                    value = pack.unpack(AttributeValueConverter.INSTANCE, null);
                    final @Nonnull SemanticType recoveredType = value.getContent().getType();
                    if (!recoveredType.equals(type)) { throw RecoveryExceptionBuilder.withMessage(Strings.format("The recovered attribute value with the type $ does not match the queried type $.", recoveredType, type)).build(); }
                    break;
                }
            } while (decoder.moveToNextRow());
        }
        return Pair.of(found, value);
    }
    
    /* -------------------------------------------------- Cache Writing -------------------------------------------------- */
    
    /**
     * Sets the cached attribute value with the given type for the given requestee.
     * 
     * @param requester the role that queried the attribute value or null for public.
     * @param requestee the identity whose cached attribute value is to be set.
     * @param expiration the time at which the cached attribute value will expire.
     * @param type the type of the attribute value which is to be set.
     * @param value the cached attribute value which is to be set.
     * @param reply the reply that returned the given attribute value.
     * 
     * @require type.isAttributeFor(requestee.getCategory()) : "The type can be used as an attribute for the category of the given requestee.";
     * @require value == null || value.isVerified() : "The attribute value is null or its signature is verified.";
     * @require value == null || value.getContent().getType().equals(type) : "The content of the given attribute value is null or matches the given type.";
     */
    @Impure
    @NonCommitting
    static void setCachedAttributeValue(@Nullable Role requester, @Nonnull InternalIdentity requestee, @Nonnull @NonNegative Time expiration, @Nonnull SemanticType type, @Nullable AttributeValue value, @Nullable Reply<?> reply) throws DatabaseException {
        Require.that(expiration.isNonNegative()).orThrow("The given time has to be non-negative but was $.");
        Require.that(type.isAttributeFor(requestee.getCategory())).orThrow("The type $ cannot be used as an attribute for the category $ of the given identity $.", type, requestee.getCategory(), requestee);
        Require.that(value == null || value.isVerified()).orThrow("The attribute value has to be null or its signature has to be verified.");
        Require.that(value == null || value.getContent().getType().equals(type)).orThrow("The content of the given attribute value has to be null or match the given type.");
        
        final @Nonnull CacheEntry entry = CacheEntryBuilder.withRequester(requester != null ? requester.getKey() : 0).withRequestee(requestee).withAttributeType(type).withFound(value != null).withExpirationTime(expiration).withAttributeValue(value != null ? Pack.pack(AttributeValueConverter.INSTANCE, value) : null).build();
        SQL.insertOrReplace(CacheEntryConverter.INSTANCE, entry, GeneralUnit.INSTANCE);
    }
    
}
