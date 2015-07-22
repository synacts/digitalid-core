package net.digitalid.core.property;

import net.digitalid.core.property.extensible.ExtensiblePropertyObserver;
import net.digitalid.core.property.indexed.IndexedPropertyObserver;
import net.digitalid.core.property.replaceable.nonnullable.NonNullableReplaceablePropertyObserver;
import net.digitalid.core.property.replaceable.nullable.NullableReplaceablePropertyObserver;

/**
 * Objects that implement this interface can be used to observe {@link ReadOnlyProperty properties}.
 * 
 * @see IndexedPropertyObserver
 * @see ExtensiblePropertyObserver
 * @see NullableReplaceablePropertyObserver
 * @see NonNullableReplaceablePropertyObserver
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface PropertyObserver {}
