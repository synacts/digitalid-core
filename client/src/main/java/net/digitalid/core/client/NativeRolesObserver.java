package net.digitalid.core.client;

import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.property.Property;
import net.digitalid.utility.property.set.SetObserver;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.client.role.NativeRole;

/**
 * Objects that implement this interface can be used to {@link Property#register(net.digitalid.utility.property.Observer) observe} {@link NativeRolesProperty native roles properties}.
 */
@Mutable
@Functional
public interface NativeRolesObserver extends SetObserver<NativeRole, ReadOnlySet<NativeRole>, DatabaseException, RecoveryException, NativeRolesObserver, NativeRolesProperty> {}
