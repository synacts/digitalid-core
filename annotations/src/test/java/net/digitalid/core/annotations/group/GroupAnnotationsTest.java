package net.digitalid.core.annotations.group;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.group.TestGroupSubclass;
import net.digitalid.utility.group.TestMemberSubclass;
import net.digitalid.utility.testing.ContractTest;

import org.junit.Test;

public class GroupAnnotationsTest extends ContractTest {

    private static final @Nonnull TestMember member = new TestMemberSubclass(new TestGroupSubclass(BigInteger.TEN));
    
    @Test
    public void testInGroup() {
        test(member::setInGroup, new TestMemberSubclass(new TestGroupSubclass(BigInteger.TEN)), new TestMemberSubclass(new TestGroupSubclass(BigInteger.ONE)));
    }
    
    @Test
    public void testInSameGroup() {
        test(member::setInSameGroup, new TestMemberSubclass(new TestGroupSubclass(BigInteger.TEN)), new TestMemberSubclass(new TestGroupSubclass(BigInteger.ONE)));
    }
    
}
