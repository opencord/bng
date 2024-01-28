/*
 * Copyright 2019-2024 Open Networking Foundation (ONF) and the ONF Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencord.bng.packets;

import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.Deserializer;
import org.onlab.packet.PacketTestUtils;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GenericPppTest {
    private Deserializer<GenericPpp> deserializer;

    private byte code = 0x1;
    private byte identifier = 0x1;
    private short length = 0x08;
    private byte[] payload = new byte[]{0x1, 0x4, 0x3, 0x4};
    private byte[] padding = new byte[]{0x0, 0x0, 0x0};

    private String packetToString = "";

    private byte[] bytes;
    private byte[] bytesPadded;

    @Before
    public void setUp() throws Exception {
        deserializer = GenericPpp.deserializer();
        ByteBuffer bb = ByteBuffer.allocate(Ppp.MIN_HEADER_LENGTH + 4);

        bb.put(code);
        bb.put(identifier);
        bb.putShort(length);
        bb.put(payload);

        bytes = bb.array();

        ByteBuffer bbPadded = ByteBuffer.allocate(Ppp.MIN_HEADER_LENGTH + 4 + 3);

        bbPadded.put(code);
        bbPadded.put(identifier);
        bbPadded.putShort(length);
        bbPadded.put(payload);
        bbPadded.put(padding);

        bytesPadded = bbPadded.array();
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
//        PacketTestUtils.testDeserializeTruncated(deserializer, bytes);
    }

    /**
     * Tests deserialize and getters.
     */
    @Test
    public void testDeserialize() throws Exception {
        GenericPpp ppp = deserializer.deserialize(bytes, 0, bytes.length);

        assertEquals(code, ppp.getCode());
        assertEquals(identifier, ppp.getIdentifier());
        assertEquals(length, ppp.getLength());
        assertArrayEquals(payload, ppp.getPayload().serialize());
    }

    /**
     * Tests deserialize with padded packet.
     */
    @Test
    public void testDeserializePadded() throws Exception {
        GenericPpp ppp = deserializer.deserialize(bytesPadded, 0, bytesPadded.length);

        assertEquals(code, ppp.getCode());
        assertEquals(identifier, ppp.getIdentifier());
        assertEquals(length, ppp.getLength());
        assertArrayEquals(payload, ppp.getPayload().serialize());
    }

}
