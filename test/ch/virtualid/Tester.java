package ch.virtualid;

import ch.virtualid.util.FreezableArrayList;
import org.junit.Assert;
import org.junit.Test;

/**
 * Code stub for testing arbitrary code snippets.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class Tester {
    
    @Test
    public void test() {
        
        FreezableArrayList<Integer> list = new FreezableArrayList<Integer>();
        
        list.clear();
        list.add(2);
        list.add(5);
        list.add(9);
        Assert.assertTrue(list.isAscending());
        Assert.assertTrue(list.isStrictlyAscending());
        Assert.assertFalse(list.isDescending());
        Assert.assertFalse(list.isStrictlyDescending());
        
        list.clear();
        list.add(5);
        list.add(5);
        list.add(9);
        Assert.assertTrue(list.isAscending());
        Assert.assertFalse(list.isStrictlyAscending());
        Assert.assertFalse(list.isDescending());
        Assert.assertFalse(list.isStrictlyDescending());
        
        list.clear();
        list.add(6);
        list.add(5);
        list.add(5);
        Assert.assertFalse(list.isAscending());
        Assert.assertFalse(list.isStrictlyAscending());
        Assert.assertTrue(list.isDescending());
        Assert.assertFalse(list.isStrictlyDescending());
        
        list.clear();
        list.add(4);
        list.add(2);
        list.add(5);
        Assert.assertFalse(list.isAscending());
        Assert.assertFalse(list.isStrictlyAscending());
        Assert.assertFalse(list.isDescending());
        Assert.assertFalse(list.isStrictlyDescending());
        
//        Identity[] identities = new Type[10];
//        long value = 4000000000l;
//        System.out.println(value);
//        System.out.println((int) value);
//        @Nonnull byte[] bytes = new byte[100];
//        Random random = new Random();
//        random.nextBytes(bytes);
//        @Nonnull Block block = new Block(null, bytes);
//        @Nonnull BigInteger hash = block.getHash();
//        System.out.println(hash.toString(16));
//        System.out.println(hash.bitLength());
//        System.out.println(hash.toByteArray().length);
//        @Nonnull Time time = new Time();
//        System.out.println(time);
//        System.out.println(time.add(Time.HOUR));
//        System.out.println(time.asInterval());
//        System.out.println(Time.DAY.asInterval());
//        System.out.println(new Time(-Time.TROPICAL_YEAR.getValue()).asDate());
//        System.out.println(new Time(-Time.TROPICAL_YEAR.getValue()).asInterval());
//        Block[] blocks = {Block.EMPTY, Block.EMPTY, new BooleanWrapper(true).toBlock()};
//        new FreezableArray<Block>(false, blocks);
//        blocks[0] = new BooleanWrapper(false).toBlock();
//        FreezableArrayList<String> list = new FreezableArrayList<String>();
//        list.add("Hello");
////        list.freeze();
//        list.add("World");
//        System.out.println(list);
//        FreezableArray<String> array = list.toFreezableArray();
//        array.set(1, array.get(1) + "!");
////        array.set(2, "Test");
//        System.out.println(array);
//        FreezableSet<String> set = new FreezableHashSet<String>();
//        set.add("Hello");
//        set.add("World");
//        System.out.println(set);
//        Object[] array = new Object[] {"Hello ", "World!"};
//        String[] strings = Arrays.copyOf(array, array.length, String[].class);
//        System.out.println(strings[0] + strings[1]);
//        
//        System.out.println(new StringWrapper("Hello World!").toBlock().getHash());
        // 18853687704169905119982974380075730378936084287165776468327974185538046445312
        // 18853687704169905119982974380075730378936084287165776468327974185538046445312
//        Database.initializeForMySQL();
//        Connection connection = Database.getConnection();
//        System.out.println(connection.getClass().getName());
//        System.out.println(new StringWrapper("").toBlock().getLength());
//        for (int i = 0; i < 20; i++) {
//            System.out.println(new SecureRandom().nextLong());
//        }
    }
    
}
