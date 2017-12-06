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
package net.digitalid.core.signature.credentials;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

import net.digitalid.core.credential.utility.ExposedExponent;
import net.digitalid.core.credential.utility.SaltedAgentPermissions;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class PublicClientCredential {
    
    @Pure
    public abstract @Nonnull ExposedExponent getExposedExponent();
    
    @Pure
    public abstract @Nonnull Element getC();
    
    @Pure
    public abstract @Nonnull Exponent getSe();
    
    @Pure
    public abstract @Nonnull Exponent getSb();
    
    @Pure
    public abstract @Nullable SaltedAgentPermissions getSaltedAgentPermissions();
    
    @Pure
    public abstract @Nullable Exponent getI();
    
    @Pure
    public abstract @Nullable Exponent getSi();
    
    @Pure
    public abstract @Nullable VerifiableEncryption getVerifiableEncryption();
    
}
