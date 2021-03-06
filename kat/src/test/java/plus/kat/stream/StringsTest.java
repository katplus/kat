package plus.kat.stream;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kraity
 */
public class StringsTest {

    @Test
    public void test_byte_array_size() {
        byte[] d0 = "kraity".getBytes(UTF_8);
        assertEquals(6, Strings.size(d0, 0, d0.length));

        byte[] d1 = "ιδΉε²".getBytes(UTF_8);
        assertEquals(3, Strings.size(d1, 0, d1.length));

        byte[] d2 = "π".getBytes(UTF_8);
        assertEquals(2, Strings.size(d2, 0, d2.length));

        byte[] d3 = "ιδΉε²+π+katplus".getBytes(UTF_8);
        assertEquals(14, Strings.size(d3, 0, d3.length));
    }

    @Test
    public void test_byte_array_to_char_array() {
        byte[] d0 = "kraity".getBytes(UTF_8);
        assertEquals("kraity", new String(Strings.toChars(d0, 0, d0.length)));

        byte[] d1 = "ιδΉε²".getBytes(UTF_8);
        assertEquals("ιδΉε²", new String(Strings.toChars(d1, 0, d1.length)));

        byte[] d2 = "π".getBytes(UTF_8);
        assertEquals("π", new String(Strings.toChars(d2, 0, d2.length)));

        byte[] d3 = "ιδΉε²+π+katplus".getBytes(UTF_8);
        assertEquals("ιδΉε²+π+katplus", new String(Strings.toChars(d3, 0, d3.length)));
    }
}
