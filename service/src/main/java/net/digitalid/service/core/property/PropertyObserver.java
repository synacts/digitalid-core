package net.digitalid.service.core.property;

import net.digitalid.service.core.property.extensible.ExtensiblePropertyObserver;
import net.digitalid.service.core.property.indexed.IndexedPropertyObserver;
import net.digitalid.service.core.property.nonnullable.NonNullablePropertyObserver;
import net.digitalid.service.core.property.nullable.NullablePropertyObserver;

/**
 * Objects that implement this interface can be used to observe {@link ReadOnlyProperty properties}.
 * 
 * @see IndexedPropertyObserver
 * @see ExtensiblePropertyObserver
 * @see NullablePropertyObserver
 * @see NonNullablePropertyObserver
 */
public interface PropertyObserver {}
