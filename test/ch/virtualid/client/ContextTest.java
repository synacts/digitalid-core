package ch.virtualid.client;

import ch.virtualid.contact.Context;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit testing of the class {@link Context}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class ContextTest {
    
    @Test
    public void isValid() {
        assertTrue(Context.isValid(0L));
        assertTrue(Context.isValid(0x01080F1080F00000L));
        assertFalse(Context.isValid(1L));
        assertFalse(Context.isValid(0x01080F0080F00000L));
    }
    
    @Test
    public void parse() throws InvalidEncodingException {
        long[] values = new long[] {0L, 0x01080F1080F00000L, -1L};
        for (long value : values) {
            @Nonnull Context context = new Context(value);
            assertEquals(context, new Context(context.toString()));
        }
    }
    
    @Test
    public void isRoot() throws InvalidEncodingException {
        assertTrue(new Context(0x0000000000000000L).isRoot());
        assertFalse(new Context(0x0100000000000000L).isRoot());
        assertFalse(new Context(0x0101000000000000L).isRoot());
        assertFalse(new Context(0x0101010000000000L).isRoot());
        assertFalse(new Context(0x0101010100000000L).isRoot());
        assertFalse(new Context(0x0101010101000000L).isRoot());
        assertFalse(new Context(0x0101010101010000L).isRoot());
        assertFalse(new Context(0x0101010101010100L).isRoot());
        assertFalse(new Context(0x0101010101010101L).isRoot());
    }
    
    @Test
    public void getLevel() throws InvalidEncodingException {
        assertTrue(new Context(0x0000000000000000L).getLevel() == 0);
        assertTrue(new Context(0x0100000000000000L).getLevel() == 1);
        assertTrue(new Context(0x0101000000000000L).getLevel() == 2);
        assertTrue(new Context(0x0101010000000000L).getLevel() == 3);
        assertTrue(new Context(0x0101010100000000L).getLevel() == 4);
        assertTrue(new Context(0x0101010101000000L).getLevel() == 5);
        assertTrue(new Context(0x0101010101010000L).getLevel() == 6);
        assertTrue(new Context(0x0101010101010100L).getLevel() == 7);
        assertTrue(new Context(0x0101010101010101L).getLevel() == 8);
    }
    
    @Test
    public void isLeaf() throws InvalidEncodingException {
        assertFalse(new Context(0x0000000000000000L).isLeaf());
        assertFalse(new Context(0x0100000000000000L).isLeaf());
        assertFalse(new Context(0x0101000000000000L).isLeaf());
        assertFalse(new Context(0x0101010000000000L).isLeaf());
        assertFalse(new Context(0x0101010100000000L).isLeaf());
        assertFalse(new Context(0x0101010101000000L).isLeaf());
        assertFalse(new Context(0x0101010101010000L).isLeaf());
        assertFalse(new Context(0x0101010101010100L).isLeaf());
        assertTrue(new Context(0x0101010101010101L).isLeaf());
    }
    
    @Test
    public void getIndex() throws InvalidEncodingException {
        assertTrue(new Context(0x0100000000000000L).getIndex() == (byte) 0x01);
        assertTrue(new Context(0x0101010101010101L).getIndex() == (byte) 0x01);
        assertTrue(new Context(0x1000000000000000L).getIndex() == (byte) 0x10);
        assertTrue(new Context(0x0101010101010110L).getIndex() == (byte) 0x10);
        assertTrue(new Context(0xFF00000000000000L).getIndex() == (byte) 0xFF);
        assertTrue(new Context(0x01010101010101FFL).getIndex() == (byte) 0xFF);
    }
    
    @Test
    public void isFirst() throws InvalidEncodingException {
        assertTrue(new Context(0x0100000000000000L).isFirst());
        assertTrue(new Context(0x0101010101010101L).isFirst());
        assertFalse(new Context(0x1000000000000000L).isFirst());
        assertFalse(new Context(0x0101010101010110L).isFirst());
        assertFalse(new Context(0xFF00000000000000L).isFirst());
        assertFalse(new Context(0x01010101010101FFL).isFirst());
    }
    
    @Test
    public void isLast() throws InvalidEncodingException {
        assertFalse(new Context(0x0100000000000000L).isLast());
        assertFalse(new Context(0x0101010101010101L).isLast());
        assertFalse(new Context(0x1000000000000000L).isLast());
        assertFalse(new Context(0x0101010101010110L).isLast());
        assertTrue(new Context(0xFF00000000000000L).isLast());
        assertTrue(new Context(0x01010101010101FFL).isLast());
    }
    
    @Test
    public void getMask() throws InvalidEncodingException {
        assertEquals(new Context(0x0000000000000000L).getMask(), 0x0000000000000000L);
        assertEquals(new Context(0x0101010100000000L).getMask(), 0xFFFFFFFF00000000L);
        assertEquals(new Context(0x0101010101010101L).getMask(), 0xFFFFFFFFFFFFFFFFL);
    }
    
    @Test
    public void isSubContextOf() throws InvalidEncodingException {
        assertTrue(new Context(0x0101010100000000L).isSubcontextOf(new Context(0x0101010100000000L)));
        assertTrue(new Context(0x0101010100000000L).isSubcontextOf(new Context(0x0101000000000000L)));
        assertFalse(new Context(0x0101000000000000L).isSubcontextOf(new Context(0x0101010100000000L)));
    }
    
    @Test
    public void isSupercontextOf() throws InvalidEncodingException {
        assertTrue(new Context(0x0101010100000000L).isSupercontextOf(new Context(0x0101010100000000L)));
        assertTrue(new Context(0x0101000000000000L).isSupercontextOf(new Context(0x0101010100000000L)));
        assertFalse(new Context(0x0101010100000000L).isSupercontextOf(new Context(0x0101000000000000L)));
    }
    
    @Test
    public void getSupercontext() throws InvalidEncodingException {
        assertEquals(new Context(0x0100000000000000L).getSupercontext(), new Context(0x0000000000000000L));
        assertEquals(new Context(0x0101010101010101L).getSupercontext(), new Context(0x0101010101010100L));
    }
    
    @Test
    public void getSupercontexts() throws InvalidEncodingException {
        @Nonnull List<Context> supercontexts = new LinkedList<Context>();
        supercontexts.add(new Context(0x0101010101010101L));
        supercontexts.add(new Context(0x0101010101010100L));
        supercontexts.add(new Context(0x0101010101010000L));
        supercontexts.add(new Context(0x0101010101000000L));
        supercontexts.add(new Context(0x0101010100000000L));
        supercontexts.add(new Context(0x0101010000000000L));
        supercontexts.add(new Context(0x0101000000000000L));
        supercontexts.add(new Context(0x0100000000000000L));
        supercontexts.add(new Context(0x0000000000000000L));
        assertEquals(new Context(0x0101010101010101L).getSupercontexts(), supercontexts);
    }
    
    @Test
    public void getNextContext() throws InvalidEncodingException {
        assertEquals(new Context(0x0101010100000000L).getNextContext(), new Context(0x0101010200000000L));
        assertEquals(new Context(0x010101FE00000000L).getNextContext(), new Context(0x010101FF00000000L));
    }
    
    @Test
    public void getPreviousContext() throws InvalidEncodingException {
        assertEquals(new Context(0x010101FF00000000L).getPreviousContext(), new Context(0x010101FE00000000L));
        assertEquals(new Context(0x0101010200000000L).getPreviousContext(), new Context(0x0101010100000000L));
    }
    
    @Test
    public void getFirstSubcontext() throws InvalidEncodingException {
        assertEquals(new Context(0x0101010100000000L).getFirstSubcontext(), new Context(0x0101010101000000L));
        assertEquals(new Context(0x010101FF00000000L).getFirstSubcontext(), new Context(0x010101FF01000000L));
    }
    
}
