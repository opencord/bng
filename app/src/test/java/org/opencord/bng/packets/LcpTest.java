package org.opencord.bng.packets;

import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.Deserializer;
import org.onlab.packet.PacketTestUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class LcpTest {
    private Deserializer<Lcp> deserializer;

    private byte code = 0x1;
    private byte identifier = 0x1;
    private short length = 0x08;
    private byte[] payload = new byte[]{0x1, 0x4, 0x3, 0x4};

    private String packetToString = "";

    private byte[] bytes;

    @Before
    public void setUp() throws Exception {
        deserializer = Lcp.deserializer();
        ByteBuffer bb = ByteBuffer.allocate(Lcp.MIN_HEADER_LENGTH + 4);

        bb.put(code);
        bb.put(identifier);
        bb.putShort(length);
        bb.put(payload);

        bytes = bb.array();
        System.out.println("ASD");

    }

    private short getTypeLength(byte type, short length) {
        return (short) ((0x7f & type) << 9 | 0x1ff & length);
    }

    @Test
    public void testDeserializeBadInput() throws Exception {
        PacketTestUtils.testDeserializeBadInput(deserializer);
    }

    @Test
    public void testDeserializeTruncated() throws Exception {
        PacketTestUtils.testDeserializeTruncated(deserializer, bytes);
    }

    /**
     * Tests deserialize and getters.
     */
    @Test
    public void testDeserialize() throws Exception {
        Lcp lcp = deserializer.deserialize(bytes, 0, bytes.length);

        assertEquals(code, lcp.getCode());
        assertEquals(identifier, lcp.getIdentifier());
        assertEquals(length, lcp.getLength());

        assertEquals(payload[0], lcp.getLcpTlvList().get(0).getType());
        assertEquals(payload[1], lcp.getLcpTlvList().get(0).getLength());
        assertTrue(Arrays.equals(
                Arrays.copyOfRange(payload, 2, payload.length),
                lcp.getLcpTlvList().get(0).getValue()));
    }

}
