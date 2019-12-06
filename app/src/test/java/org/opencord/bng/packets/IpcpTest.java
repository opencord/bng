/*
 * Copyright 2019-present Open Networking Foundation
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
import org.onlab.packet.IpAddress;
import org.onlab.packet.PacketTestUtils;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IpcpTest {

    private Deserializer<Ipcp> deserializer;

    private byte ipAddressTlvSize = 0x6;
    private IpAddress ipAddress = IpAddress.valueOf("10.0.0.1");

    private byte optionalTlvSize = 0x6;
    private byte optionalTlvType = 0x01;
    private byte[] optionalTlvValue = new byte[]{0x4, 0x3, 0x2, 0x1};

    private byte code = Ipcp.CONF_REQ;
    private byte identifier = 0x1;
    private short length = (short) (Ipcp.MIN_HEADER_LENGTH + ipAddressTlvSize + optionalTlvSize);


    private byte[] bytes;

    @Before
    public void setUp() throws Exception {
        deserializer = Ipcp.deserializer();
        ByteBuffer bb = ByteBuffer.allocate(Ipcp.MIN_HEADER_LENGTH + ipAddressTlvSize + optionalTlvSize);

        bb.put(code);
        bb.put(identifier);
        bb.putShort(length);

        bb.put(PppTlv.IPCPTLV_IP_ADDRESS);
        bb.put(ipAddressTlvSize);
        bb.put(ipAddress.toOctets());

        bb.put(optionalTlvType);
        bb.put(optionalTlvSize);
        bb.put(optionalTlvValue);

        bytes = bb.array();
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
        Ipcp ipcp = deserializer.deserialize(bytes, 0, bytes.length);
        PppTlv optionalTlv = ipcp.getIpcpTlvList().get(0);

        assertEquals(code, ipcp.getCode());
        assertEquals(identifier, ipcp.getIdentifier());
        assertEquals(length, ipcp.getLength());

        assertEquals(optionalTlvType, optionalTlv.getType());
        assertEquals(optionalTlvSize, optionalTlv.getLength());
        assertArrayEquals(optionalTlvValue, optionalTlv.getValue());

        assertEquals(PppTlv.IPCPTLV_IP_ADDRESS, ipcp.getIpAddressTlv().getType());
        assertEquals(ipAddressTlvSize, ipcp.getIpAddressTlv().getLength());
        assertEquals(ipAddress, IpAddress.valueOf(IpAddress.Version.INET,
                                                  ipcp.getIpAddressTlv().getValue()));
        assertEquals(ipAddress, ipcp.getIpAddress());
    }
}
