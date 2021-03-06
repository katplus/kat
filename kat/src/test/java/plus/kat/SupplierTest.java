package plus.kat;

import org.junit.jupiter.api.Test;
import plus.kat.anno.Expose;
import plus.kat.spare.IterableSpare;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kraity
 */
@SuppressWarnings("unchecked")
public class SupplierTest {

    @Test
    public void test_embed1() {
        String[] list = new String[]{
            "java.util.Map",
            "java.util.Set",
            "java.util.List",
            "java.lang.Object"
        };

        Supplier supplier = Supplier.ins();
        for (String o : list) {
            assertNotNull(
                supplier.lookup(o)
            );
        }
    }

    @Test
    public void test() {
        Supplier supplier = Supplier.ins();

        Meta meta = supplier.read(
            Meta.class, new Event<>(
                "{:id(1):tag{:1(2):3(4)}}"
            )
        );

        assertNotNull(meta);
        assertEquals(1, meta.id);
        assertNotNull(meta.tag);
        assertEquals(2L, meta.tag.get(1));
        assertEquals(4L, meta.tag.get(3));
    }

    @Test
    public void test_read_Array() {
        Supplier supplier = Supplier.ins();

        HashMap<Job, String> data0 = new HashMap<>();
        data0.put(
            Job.KAT, "{{:id(0):name(kraity)}{:id(1):name(kraity)}{:id(2):name(kraity)}{:id(3):name(kraity)}}"
        );
        data0.put(
            Job.JSON, "[{\"id\":0,\"name\":\"kraity\"},{\"id\":1,\"name\":\"kraity\"},{\"id\":2,\"name\":\"kraity\"},{\"id\":3,\"name\":\"kraity\"}]"
        );
        data0.put(
            Job.DOC, "<data><user><id>0</id><name>kraity</name></user><user><id>1</id><name>kraity</name></user><user><id>2</id><name>kraity</name></user><user><id>3</id><name>kraity</name></user></data>"
        );

        for (Map.Entry<Job, String> entry : data0.entrySet()) {
            User[] users0 = supplier.solve(
                User[].class,
                entry.getKey(),
                new Event<>(
                    entry.getValue()
                )
            );
            assertNotNull(users0);
            assertEquals(4, users0.length);
            for (int i = 0; i < users0.length; i++) {
                User user = users0[i];
                assertNotNull(user);
                assertEquals(i, user.id);
                assertEquals("kraity", user.name);
            }

            List<User> users1 = supplier.solve(
                List.class, entry.getKey(),
                new Event<List<User>>(
                    entry.getValue()
                ) {
                }
            );
            assertNotNull(users1);
            assertEquals(4, users1.size());
            for (int i = 0; i < users1.size(); i++) {
                User user = users1.get(i);
                assertNotNull(user);
                assertEquals(i, user.id);
                assertEquals("kraity", user.name);
            }
        }

        HashMap<Job, String> data1 = new HashMap<>();
        data1.put(
            Job.KAT, "{(0)(1)(2)(3)}"
        );
        data1.put(
            Job.JSON, "[0,1,2,3]"
        );
        data1.put(
            Job.DOC, "<data><item>0</item><item>1</item><item>2</item><item>3</item></data>"
        );

        for (Map.Entry<Job, String> entry : data1.entrySet()) {
            int[] d0 = supplier.solve(
                int[].class,
                entry.getKey(),
                new Event<>(
                    entry.getValue()
                )
            );
            assertNotNull(d0);
            assertEquals(4, d0.length);
            for (int i = 0; i < d0.length; i++) {
                assertEquals(i, d0[i]);
            }

            long[] d1 = supplier.solve(
                long[].class,
                entry.getKey(),
                new Event<>(
                    entry.getValue()
                )
            );
            assertNotNull(d1);
            assertEquals(4, d1.length);
            for (long i = 0; i < d1.length; i++) {
                assertEquals(i, d1[(int) i]);
            }

            short[] d2 = supplier.solve(
                short[].class,
                entry.getKey(),
                new Event<>(
                    entry.getValue()
                )
            );
            assertNotNull(d2);
            assertEquals(4, d2.length);
            for (short i = 0; i < d2.length; i++) {
                assertEquals(i, d2[i]);
            }

            boolean[] d3 = supplier.solve(
                boolean[].class,
                entry.getKey(),
                new Event<>(
                    entry.getValue()
                )
            );
            assertNotNull(d3);
            assertEquals(4, d3.length);
            for (int i = 0; i < d3.length; i++) {
                assertEquals(i == 1, d3[i]);
            }

            for (Class<?> klass : new Class[]{
                Integer[].class, Long[].class, Short[].class
            }) {
                Object e0 = supplier.solve(
                    klass, entry.getKey(),
                    new Event<>(
                        entry.getValue()
                    )
                );
                assertNotNull(e0);

                int size = Array.getLength(e0);
                assertEquals(4, size);

                Class<?> type = klass.getComponentType();
                for (int i = 0; i < size; i++) {
                    Object d = Array.get(e0, i);
                    assertNotNull(d);
                    assertSame(type, d.getClass());
                    assertEquals(i, ((Number) d).intValue());
                }

                List<?> e1 = supplier.solve(
                    List.class, entry.getKey(),
                    new Event<List<?>>(
                        entry.getValue()
                    ).with(
                        new ParameterizedType() {
                            @Override
                            public Type getRawType() {
                                return List.class;
                            }

                            @Override
                            public Type getOwnerType() {
                                return null;
                            }

                            @Override
                            public Type[] getActualTypeArguments() {
                                return new Type[]{type};
                            }
                        }
                    )
                );
                assertNotNull(e1);
                assertEquals(4, e1.size());
                for (int i = 0; i < e1.size(); i++) {
                    Object d = e1.get(i);
                    assertNotNull(d);
                    assertSame(type, d.getClass());
                    assertEquals(i, ((Number) d).intValue());
                }
            }
        }
    }

    @Test
    public void test_read_Map() {
        Supplier supplier = Supplier.ins();

        Class<Map<String, Object>>[] cls = new Class[]{
            AbstractMap.class,
            Map.class,
            HashMap.class,
            LinkedHashMap.class,
            TreeMap.class,
            Hashtable.class,
            WeakHashMap.class,
            ConcurrentMap.class,
            NavigableMap.class,
            Properties.class,
            ConcurrentHashMap.class,
            ConcurrentNavigableMap.class,
            ConcurrentSkipListMap.class
        };

        for (Class<Map<String, Object>> klass : cls) {
            Map<String, Object> map = supplier.read(
                klass, new Event<>(
                    "{i:id(1)s:tag(kat)}"
                )
            );

            assertNotNull(map);
            assertTrue(klass.isInstance(map));
            assertEquals(1, map.get("id"));
            assertEquals("kat", map.get("tag"));
        }
    }

    @Test
    public void test_read_Set() {
        Supplier supplier = Supplier.ins();

        Class<Set<Object>>[] cls = new Class[]{
            AbstractSet.class,
            Set.class,
            HashSet.class,
            LinkedHashSet.class,
            TreeSet.class,
            SortedSet.class,
            NavigableSet.class,
            ConcurrentSkipListSet.class
        };

        for (Class<Set<Object>> klass : cls) {
            Set<Object> set = supplier.read(
                klass, new Event<>(
                    "{s(1)s(kat)}"
                )
            );

            assertNotNull(set);
            assertTrue(klass.isInstance(set));
            assertTrue(set.contains("1"));
            assertTrue(set.contains("kat"));
            assertFalse(set.contains("plus"));
        }
    }

    @Test
    public void test_read_List() {
        Supplier supplier = Supplier.ins();

        Class<List<Object>>[] cls = new Class[]{
            AbstractList.class,
            List.class,
            ArrayList.class,
            Stack.class,
            Vector.class,
            LinkedList.class,
            CopyOnWriteArrayList.class
        };

        for (Class<List<Object>> klass : cls) {
            List<Object> list = supplier.read(
                klass, new Event<>(
                    "{i(1)s(kat)}"
                )
            );

            assertNotNull(list);
            assertTrue(klass.isInstance(list));
            assertEquals(1, list.get(0));
            assertEquals("kat", list.get(1));
        }
    }

    @Test
    public void test_read_Iterable() {
        IterableSpare spare = IterableSpare.INSTANCE;

        Class<Iterable<Object>>[] cls = new Class[]{
            Iterable.class,
            ArrayList.class,
            HashSet.class,
            Collection.class,
            Deque.class,
            ArrayDeque.class
        };

        for (Class<Iterable<Object>> klass : cls) {
            Iterable<Object> iterable = spare.read(
                new Event<Iterable<Object>>(
                    "{i(1)s(kat)}"
                ).with(
                    klass
                )
            );

            assertNotNull(iterable);
            assertTrue(klass.isInstance(iterable));

            Iterator<?> it = iterable.iterator();

            assertTrue(it.hasNext());
            assertEquals(1, it.next());

            assertTrue(it.hasNext());
            assertEquals("kat", it.next());
        }

        Class<Iterable<Object>>[] cls2 = new Class[]{
            Queue.class,
            PriorityQueue.class,
            AbstractQueue.class
        };

        for (Class<Iterable<Object>> klass : cls2) {
            Iterable<Object> iterable = spare.read(
                new Event<Iterable<Object>>(
                    "{i(123)i(456)}"
                ).with(
                    klass
                )
            );

            assertNotNull(iterable);
            assertTrue(klass.isInstance(iterable));

            Iterator<?> it = iterable.iterator();

            assertTrue(it.hasNext());
            assertEquals(123, it.next());

            assertTrue(it.hasNext());
            assertEquals(456, it.next());
        }
    }

    static class User {
        @Expose("id")
        private int id;

        @Expose("name")
        private String name;

        @Override
        public String toString() {
            return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
        }
    }

    static class Meta {
        private int id;
        private Map<Integer, Long> tag;

        public Meta(
            @Expose("id") int id,
            @Expose("tag") Map<Integer, Long> tag
        ) {
            this.id = id;
            this.tag = tag;
        }
    }
}
