package net.openhft.chronicle.coder;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WordsEncoderTest {

    @Test
    public void parseLatLon() throws IOException {
        for (int size : new int[]{16, 36, /*short*/1024, /*medium*/1936, 4096}) {
            System.out.println("size: " + size);
            Coder coder = WordsCoderBuilder.fromFile("common-words.txt", size).build();
            for (int x = -90; x <= 90; x += 15) {
                for (int y = -180; y <= 180; y += 15) {
                    StringBuilder sb = new StringBuilder();
                    coder.appendLatLon(sb, x, y, 2e-3);
//                    System.out.println("x: " + x + ", y: " + y + " " + sb);
                    Coder.LatLon latLon = coder.parseLatLon(sb);
                    assertEquals(x, latLon.latitude, 1e-3);
                    assertEquals(y, latLon.longitude, 1e-3);
                    assertTrue(latLon.precision >= 2e-3 / size);
                    assertTrue(latLon.precision <= 2e-3);
                }
            }
        }
    }

    @Test
    public void parseLongDecimal() throws IOException {
        Coder coder = WordsCoderBuilder.fromFile("common-words.txt", 16).build();
        StringBuilder sb = new StringBuilder();
        for (long l : new long[]{
                Long.MIN_VALUE, 0xFEDCBA9876543210L, -Long.MAX_VALUE, Integer.MIN_VALUE, -1,
                0, 1, Integer.MAX_VALUE, 0x0123456789ABCDEFL, Long.MAX_VALUE}) {
            sb.setLength(0);
            coder.appendLong(sb, l);
            assertEquals(l, coder.parseLong(sb));
        }
    }
}