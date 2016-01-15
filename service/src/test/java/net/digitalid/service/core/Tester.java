package net.digitalid.service.core;

import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;

import org.junit.Test;

class A {}

class B extends A {}

class C extends A {}

class Other<O> {
    
    @SuppressWarnings("unchecked")
    O castTo(Object object) {
        try {
            return (O) object;
        } catch (ClassCastException exception) {
            System.out.println("Problem in castTo(): " + exception);
            return null;
        }
    }
    
}

/**
 * Code stub for testing arbitrary code snippets.
 */
public class Tester {
    
    @Test
    public void test() {
        Other<B> other = new Other<>();
        try {
            B b;
            b = other.castTo(new B());
            b = other.castTo(new C());
        } catch (ClassCastException exception) {
            System.out.println("Problem in test(): " + exception);
        }
        
        FreezableArray<B> array = FreezableArray.get(4);
        array.set(0, new B());
        
        FreezableLinkedList<B> list = FreezableLinkedList.get(new B(), new B());
        FreezableArray<B> freezableArray = list.toFreezableArray();
        freezableArray.set(0, new B());
    }
    
}
