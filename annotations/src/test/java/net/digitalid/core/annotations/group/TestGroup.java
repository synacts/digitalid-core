package net.digitalid.core.annotations.group;

import net.digitalid.core.annotations.group.GroupMember;
import net.digitalid.core.annotations.group.GroupInterface;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

import net.digitalid.core.annotations.group.InSameGroup;

import net.digitalid.utility.validation.annotations.testing.IncorrectUsage;
import net.digitalid.utility.validation.annotations.type.Immutable;

@Immutable
@GenerateSubclass
public interface TestGroup extends GroupInterface {
    
    @Pure
    public default @IncorrectUsage(InSameGroup.Validator.class) GroupMember getMember() {
        return null;
    }
    
}
