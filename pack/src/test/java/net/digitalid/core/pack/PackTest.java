package net.digitalid.core.pack;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.time.Time;

import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class PackTest extends CoreTest {
    
    public static final @Nonnull @Loaded SemanticType NAME = SemanticType.map("name@core.digitalid.net").load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.STRING).withCategories(Category.INTERNAL_NON_HOST_IDENTITIES).withCachingPeriod(Time.MONTH).build());
    
    @Test
    public void testEquals() {
        final @Nonnull Pack pack1 = Pack.pack(StringConverter.INSTANCE, "Test", NAME);
        final @Nonnull Pack pack2 = Pack.pack(StringConverter.INSTANCE, "Test", NAME);
        assertThat(pack1).isEqualTo(pack2);
    }
    
}
