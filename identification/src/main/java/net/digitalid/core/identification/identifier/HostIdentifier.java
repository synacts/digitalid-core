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
package net.digitalid.core.identification.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.equality.Unequal;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.IdentifierResolver;

/**
 * This interface models host identifiers.
 * 
 * (This type has to be a class because otherwise the static {@link #isValid(java.lang.String)} method would not be inherited by the generated subclass.)
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class HostIdentifier extends InternalIdentifier {
    
    /* -------------------------------------------------- Digital ID Core Identifier -------------------------------------------------- */
    
    /**
     * Stores the host identifier {@code core.digitalid.net}.
     */
    public final static @Nonnull HostIdentifier DIGITALID = HostIdentifier.with("core.digitalid.net");
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string is a valid host identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return InternalIdentifier.isConforming(string) && !string.contains("@");
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns a host identifier with the given string.
     */
    @Pure
    public static @Nonnull HostIdentifier with(@Nonnull @Valid String string) {
        return new HostIdentifierSubclass(string);
    }
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull HostIdentity resolve() throws ExternalException {
        return IdentifierResolver.configuration.get().resolve(this).castTo(HostIdentity.class);
    }
    
    /* -------------------------------------------------- Host Identifier -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getHostIdentifier() {
        return this;
    }
    
    /* -------------------------------------------------- Host Name -------------------------------------------------- */
    
    /**
     * Returns this host identifier as a host name which can be used as a prefix in database tables.
     * Host identifiers consist of at most 38 characters. If the identifier starts with a digit, the host name is prepended by an underscore.
     */
    @Pure
    @TODO(task = "If we take the host name as the database name and not as table prefixes, then we might be able to increase the length restrictions.", date = "2016-06-19", author = Author.KASPAR_ETTER)
    public @Nonnull @CodeIdentifier @MaxSize(61) @Unequal("general") String asSchemaName() {
        final @Nonnull String string = getString();
        return (Character.isDigit(string.charAt(0)) ? "_" : "") + string.replace(".", "_").replace("-", "$");
    }
    
}
