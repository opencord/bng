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

import static org.junit.Assert.assertEquals;

public class PppoeTest {
    private Deserializer<Pppoe> deserializer;

    private byte versionAndType = 0x11;
    private byte code = 0x9;
    private byte codeSession = 0x0;
    private short sessionId = 0x0102;
    private short payloadLength = 0;

    private byte[] padding = new byte[]{0x0, 0x0, 0x0};

    private byte[] bytesDisc;
    private byte[] bytesPadded;
    private byte[] bytesSession;

    @Before
    public void setUp() throws Exception {
        deserializer = Pppoe.deserializer();
        ByteBuffer bb = ByteBuffer.allocate(Pppoe.HEADER_LENGTH);

        bb.put(versionAndType);
        bb.put(code);
        bb.putShort(sessionId);
        bb.putShort(payloadLength);

        bytesDisc = bb.array();
        ByteBuffer bbPadded = ByteBuffer.allocate(Pppoe.HEADER_LENGTH + 3);

        bbPadded.put(versionAndType);
        bbPadded.put(code);
        bbPadded.putShort(sessionId);
        bbPadded.putShort(payloadLength);
        bbPadded.put(padding);

        bytesPadded = bbPadded.array();

        ByteBuffer bbSession = ByteBuffer.allocate(Pppoe.HEADER_LENGTH + 2);

        bbSession.put(versionAndType);
        bbSession.put(codeSession);
        bbSession.putShort(sessionId);
        bbSession.putShort((short) (payloadLength + 2));
        bbSession.putShort(PppProtocolType.LCP.code());

        bytesSession = bbSession.array();
    }

    @Test
    public void testDeserializeBadInput() throws Exception {
        PacketTestUtils.testDeserializeBadInput(deserializer);
    }

    @Test
    public void testDeserializeTruncated() throws Exception {
        PacketTestUtils.testDeserializeTruncated(deserializer, bytesDisc);
    }

    /**
     * Tests deserialize and getters.
     */
    @Test
    public void testDeserializeDiscovery() throws Exception {
        Pppoe pppoe = deserializer.deserialize(bytesDisc, 0, bytesDisc.length);

        assertEquals(versionAndType, (pppoe.getVersion() << 4) | pppoe.getTypeId());
        assertEquals(code, pppoe.getPacketType().code());
        assertEquals(sessionId, pppoe.getSessionId());
        assertEquals(payloadLength, pppoe.getPayloadLength());
    }

    /**
     * Tests deserialize with padded packet.
     */
    @Test
    public void testDeserializePadded() throws Exception {
        Pppoe pppoe = deserializer.deserialize(bytesPadded, 0, bytesPadded.length);

        assertEquals(versionAndType, (pppoe.getVersion() << 4) | pppoe.getTypeId());
        assertEquals(code, pppoe.getPacketType().code());
        assertEquals(sessionId, pppoe.getSessionId());
        assertEquals(payloadLength, pppoe.getPayloadLength());
    }

    /**
     * Tests deserialize of PPPoE session packets.
     */
    @Test
    public void testDeserializeSession() throws Exception {
        Pppoe pppoe = deserializer.deserialize(bytesSession, 0, bytesSession.length);

        assertEquals(versionAndType, (pppoe.getVersion() << 4) | pppoe.getTypeId());
        assertEquals(codeSession, pppoe.getPacketType().code());
        assertEquals(sessionId, pppoe.getSessionId());
        assertEquals(payloadLength + 2, pppoe.getPayloadLength());
        assertEquals(PppProtocolType.LCP.code(), pppoe.getPppProtocol());
    }
}
