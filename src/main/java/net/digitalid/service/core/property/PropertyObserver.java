package net.digitalid.core.property;

import net.digitalid.core.property.extensible.ExtensiblePropertyObserver;
import net.digitalid.core.property.indexed.IndexedPropertyObserver;
import net.digitalid.core.property.nonnullable.NonNullablePropertyObserver;
import net.digitalid.core.property.nullable.NullablePropertyObserver;

/**
 * Objects that implement this interface can be used to observe {@link ReadOnlyProperty properties}.
 * 
 * @see IndexedPropertyObserver
 * @see ExtensiblePropertyObserver
 * @see NullablePropertyObserver
 * @see NonNullablePropertyObserver
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public interface PropertyObserver {}
