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
//package net.digitalid.core.keychain;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.digitalid.utility.annotations.method.Pure;
//import net.digitalid.utility.annotations.ownership.NonCaptured;
//import net.digitalid.utility.annotations.ownership.Shared;
//import net.digitalid.utility.annotations.parameter.Modified;
//import net.digitalid.utility.annotations.parameter.Unmodified;
//import net.digitalid.utility.conversion.enumerations.Representation;
//import net.digitalid.utility.conversion.exceptions.ConnectionException;
//import net.digitalid.utility.conversion.exceptions.RecoveryException;
//import net.digitalid.utility.conversion.interfaces.Converter;
//import net.digitalid.utility.conversion.interfaces.Decoder;
//import net.digitalid.utility.conversion.interfaces.Encoder;
//import net.digitalid.utility.conversion.interfaces.GenericTypeConverter;
//import net.digitalid.utility.conversion.model.CustomAnnotation;
//import net.digitalid.utility.conversion.model.CustomField;
//import net.digitalid.utility.conversion.model.CustomType;
//import net.digitalid.utility.immutable.ImmutableList;
//import net.digitalid.utility.immutable.ImmutableMap;
//import net.digitalid.utility.time.TimeConverter;
//
//import static net.digitalid.utility.conversion.model.CustomType.BOOLEAN;
//
///**
// *
// */
//public abstract class KeyChainItemConverter<OBJECT> implements GenericTypeConverter<OBJECT, KeyChainItem<OBJECT>, Void> {
//    
//    @Override
//    public abstract @Nonnull Converter<OBJECT, Void> getObjectConverter();
//    
//    @Override
//    public @Nonnull Class<? super KeyChainItem<OBJECT>> getType() {
//        return KeyChainItem.class;
//    }
//    
//    @Override
//    public @Nonnull String getTypeName() {
//        return "KeyChainItem";
//    }
//    
//    @Override
//    public @Nonnull String getTypePackage() {
//        return "net.digitalid.core.keychain";
//    }
//    
//    private static final @Nonnull ImmutableList<CustomField> fields;
//    
//    static {
//        final @Nonnull Map<@Nonnull String, @Nullable Object> frozenPure = new HashMap<>();
//        
//        fields = ImmutableList.withElements(CustomField.with(CustomType.TUPLE.of(TimeConverter.INSTANCE), "time", ImmutableList.<CustomAnnotation>withElements(CustomAnnotation.with(Pure.class, ImmutableMap.withMappingsOf(frozenPure)))), CustomField.with(CustomType.TUPLE.of(TimeConverter.INSTANCE), "key", ImmutableList.<CustomAnnotation>withElements(CustomAnnotation.with(Pure.class, ImmutableMap.withMappingsOf(frozenPure)))));
//    }
//    
//    @Override
//    public @Nonnull ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
//        return null;
//    }
//    
//    @Override
//    public <EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified KeyChainItem<OBJECT> object, @NonCaptured @Modified Encoder<EXCEPTION> encoder) throws EXCEPTION {
//        
//    }
//    
//    @Override
//    public <EXCEPTION extends ConnectionException> KeyChainItem<OBJECT> recover(@NonCaptured @Modified Decoder<EXCEPTION> decoder, @Shared Void aVoid) throws EXCEPTION, RecoveryException {
//        return null;
//    }
//    
//}
