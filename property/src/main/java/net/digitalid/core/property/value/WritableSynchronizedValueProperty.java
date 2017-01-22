package net.digitalid.core.property.value;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.Captured;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Mutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeBuilder;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.value.PersistentValueObserver;
import net.digitalid.database.property.value.PersistentValuePropertyEntry;
import net.digitalid.database.property.value.PersistentValuePropertyEntryBuilder;
import net.digitalid.database.property.value.ReadOnlyPersistentValueProperty;
import net.digitalid.database.property.value.WritablePersistentValuePropertyImplementation;

import net.digitalid.core.subject.CoreSubject;
import net.digitalid.core.entity.CoreSite;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.property.SynchronizedProperty;
import net.digitalid.core.synchronizer.Synchronizer;

/**
 * This synchronized property synchronizes a value across sites.
 */
@ThreadSafe
@GenerateBuilder
@GenerateSubclass
@Mutable(ReadOnlyPersistentValueProperty.class)
public abstract class WritableSynchronizedValueProperty<ENTITY extends Entity<?>, KEY, CONCEPT extends CoreSubject<ENTITY, KEY>, VALUE> extends WritablePersistentValuePropertyImplementation<CoreSite<?>, CONCEPT, VALUE> implements SynchronizedProperty<ENTITY, KEY, CONCEPT, PersistentValuePropertyEntry<CONCEPT, VALUE>, PersistentValueObserver<CONCEPT, VALUE>> {
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull SynchronizedValuePropertyTable<ENTITY, KEY, CONCEPT, VALUE, ?> getTable();
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    @Impure
    @Override
    @Committing
    public @Capturable @Valid VALUE set(@Captured @Valid VALUE newValue) throws DatabaseException, ReentranceException {
        lock.lock();
        try {
            if (!loaded) { load(false); }
            final @Valid VALUE oldValue = value;
            if (!Objects.equals(newValue, oldValue)) {
                final @Nullable Time oldTime = time;
                final @Nonnull Time newTime = TimeBuilder.build();
                Synchronizer.execute(ValuePropertyInternalActionBuilder.withProperty(this).withOldValue(oldValue).withNewValue(newValue).withOldTime(oldTime).withNewTime(newTime).build());
            }
            return oldValue;
        } finally {
            lock.unlock();
        }
    }
    
    /* -------------------------------------------------- Action -------------------------------------------------- */
    
    /**
     * Replaces the time and value of this property from the {@link ValuePropertyInternalAction}.
     * 
     * @require !Objects.equals(newValue, oldValue) : "The new value may not be the same as the old value.";
     */
    @Impure
    @NonCommitting
    @TODO(task = "Implement and use SQL.insertOrUpdate() instead of using SQL.insert().", date = "2016-11-10", author = Author.KASPAR_ETTER, assignee = Author.STEPHANIE_STROKA, priority = Priority.HIGH)
    protected void replace(@Nullable Time oldTime, @Nullable Time newTime, @Valid VALUE oldValue, @Valid VALUE newValue) throws DatabaseException {
        lock.getReentrantLock().lock();
        try {
            final @Nonnull PersistentValuePropertyEntry<CONCEPT, VALUE> entry = PersistentValuePropertyEntryBuilder.<CONCEPT, VALUE>withSubject(getSubject()).withTime(newTime).withValue(newValue).build();
            SQL.insert(entry, getTable().getEntryConverter(), getSubject().getSite()); // TODO: Only update if the oldTime and oldValue match the replaced entry.
            this.time = newTime;
            this.value = newValue;
            notifyObservers(oldValue, newValue);
        } finally {
            lock.unlock();
        }
    }
    
}
