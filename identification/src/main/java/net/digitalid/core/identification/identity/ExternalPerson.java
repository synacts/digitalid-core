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
package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.validation.annotations.type.Mutable;
import net.digitalid.utility.validation.annotations.value.Invariant;


/**
 * This class models an external person.
 * 
 * @see EmailPerson
 * @see MobilePerson
 */
@Mutable
public abstract class ExternalPerson extends Person {
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    /**
     * Sets the category of this external person.
     */
    @Impure
    abstract void setCategory(@Nonnull @Invariant(condition = "category.isInternalPerson()", message = "The category has to denote an internal person.") Category category);
    
}
