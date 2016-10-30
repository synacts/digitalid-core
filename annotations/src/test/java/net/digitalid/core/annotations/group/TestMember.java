package net.digitalid.core.annotations.group;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.type.Mutable;

@Mutable
@GenerateSubclass
public abstract class TestMember extends RootClass implements GroupMember {
    
    @Pure
    public @Nonnull GroupInterface getSpecificGroup() {
        return getGroup();
    }
    
    @Impure
    public void setInGroup(@InGroup("getSpecificGroup()") GroupMember member) {}
    
    @Impure
    public void setInSameGroup(@InSameGroup GroupMember member) {}
    
}
