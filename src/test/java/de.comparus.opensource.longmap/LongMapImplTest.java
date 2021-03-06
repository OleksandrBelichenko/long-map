package de.comparus.opensource.longmap;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Oleksandr Belichenko
 */
public class LongMapImplTest {

    private LongMap<String> strings;
    private static final String FIRST_VALUE = "first";
    private static final String SECOND_VALUE = "second";
    private static final String NULL_VALUE = null;

    @Before
    public void setUp() {
        strings = new LongMapImpl<>();
    }

    @Test
    public void initLongMapWithDefaults() {
        int size = 0;
        assertNotNull(strings.keys());
        assertNull(strings.values());
        assertEquals(size, strings.size());
        assertTrue(strings.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void initLongMapWithLoadFactor() {
        LongMap<String> map = new LongMapImpl<>(-3f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initLongMapWithInitCapacity() {
        LongMap<String> map = new LongMapImpl<>(-5);
    }

    @Test
    public void put() {
        String check = strings.put(1, FIRST_VALUE);
        assertNull(check);

        check = strings.put(1, SECOND_VALUE);
        assertEquals(FIRST_VALUE, check);
    }

    @Test
    public void get() {
        strings.put(1, FIRST_VALUE);
        assertEquals(FIRST_VALUE, strings.get(1));

        strings.put(1, SECOND_VALUE);
        assertEquals(SECOND_VALUE, strings.get(1));

        assertNull(strings.get(2));

        strings.put(2, NULL_VALUE);
        assertNull(strings.get(2));
    }

    @Test
    public void remove() {
        assertNull(strings.remove(1));

        strings.put(1, FIRST_VALUE);
        assertEquals(FIRST_VALUE, strings.remove(1));
    }

    @Test
    public void isEmpty() {
        assertTrue(strings.isEmpty());
        strings.put(1, FIRST_VALUE);
        assertFalse(strings.isEmpty());
    }

    @Test
    public void containsKey() {
        assertFalse(strings.containsKey(1));
        strings.put(1, FIRST_VALUE);
        assertTrue(strings.containsKey(1));
    }

    @Test
    public void containsValue() {
        assertFalse(strings.containsValue(FIRST_VALUE));
        strings.put(1, FIRST_VALUE);
        assertTrue(strings.containsValue(FIRST_VALUE));
    }

    @Test
    public void keys() {
        int lenght = 0;
        assertEquals(lenght, strings.keys().length);

        strings.put(1, FIRST_VALUE);
        strings.put(2, SECOND_VALUE);
        strings.put(3, NULL_VALUE);

        long[] keys = new long[3];
        keys[0] = 1;
        keys[1] = 2;
        keys[2] = 3;
        assertArrayEquals(keys, strings.keys());

        strings.put(3, FIRST_VALUE);
        assertArrayEquals(keys, strings.keys());

        strings.remove(1);
        keys = new long[2];
        keys[0] = 2;
        keys[1] = 3;
        assertArrayEquals(keys, strings.keys());
    }

    @Test
    public void values() {
        assertNull(strings.values());

        strings.put(1, FIRST_VALUE);
        strings.put(2, SECOND_VALUE);
        strings.put(3, NULL_VALUE);

        String[] values = new String[3];
        values[0] = FIRST_VALUE;
        values[1] = SECOND_VALUE;
        values[2] = NULL_VALUE;
        assertArrayEquals(values, strings.values());

        strings.put(3, FIRST_VALUE);
        values[2] = FIRST_VALUE;
        assertArrayEquals(values, strings.values());

        strings.remove(1);
        values = new String[2];
        values[0] = SECOND_VALUE;
        values[1] = FIRST_VALUE;
        assertArrayEquals(values, strings.values());
    }

    @Test
    public void size() {
        int size = 0;
        assertEquals(size, strings.size());
        strings.put(1, FIRST_VALUE);
        assertEquals(++size, strings.size());

        strings.put(1, SECOND_VALUE);
        assertEquals(size, strings.size());

        strings.remove(1);
        assertEquals(--size, strings.size());

        strings.get(2);
        assertEquals(size, strings.size());

        size = 0;
        strings.clear();
        assertEquals(size, strings.size());
    }

    @Test
    public void clear() {
        strings.put(1, FIRST_VALUE);
        strings.put(2, SECOND_VALUE);
        strings.put(3, NULL_VALUE);

        strings.clear();
        int size = 0;
        assertEquals(size, strings.size());
    }
}