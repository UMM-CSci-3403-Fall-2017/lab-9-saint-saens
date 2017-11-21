package segmentedfilesystem;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * This is just a stub test file. You should rename it to
 * something meaningful in your context and populate it with
 * useful tests.
 */
public class DummyTest {

    @Test
    public void test() {
        fail("Not yet implemented");
    }

    @Test
    public void bitwiseTest(){
        byte byt = 2;
        byte bite = 104;
        int shift = bite<<8;
        int res = shift + byt;
        assertEquals(26624, shift);
        assertEquals(26626, res);
    }
}
