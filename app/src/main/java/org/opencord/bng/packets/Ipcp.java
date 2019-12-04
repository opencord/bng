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

import com.google.common.base.Objects;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.Deserializer;
import org.onlab.packet.IpAddress;

import java.nio.ByteBuffer;
import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.onlab.packet.PacketUtils.checkHeaderLength;
import static org.onlab.packet.PacketUtils.checkInput;

/**
 * Implements the IP Control Protocol (IPCP) packet format.
 */

public class Ipcp extends Ppp {

    static final int MIN_HEADER_LENGTH = 4;

    public static final byte CONF_REQ = 0x01;
    public static final byte ACK = 0x02;
    public static final byte NAK = 0x03;

    protected PppTlv ipaddresstlv = null;


    /**
     * Get the IP Address TLV field.
     *
     * @return the IP Address TLV field if present, null otherwise
     */
    public PppTlv getIpAddressTlv() {
        return this.ipaddresstlv;
    }

    /**
     * Get the IP address TLV field as an IpAddress object.
     *
     * @return The IP address TLV field as IpAddress object, it will be address
     * 0 if no TLV address is present in the packet
     */
    public IpAddress getIpAddress() {
        return IpAddress.valueOf(IpAddress.Version.INET,
                                 this.getIpAddressTlv().getValue());
    }

    /**
     * Set the IP address TLV field.
     *
     * @param ipaddresstlv the IP address TLV to set
     * @return this
     */
    public Ipcp setIpAddressTlv(PppTlv ipaddresstlv) {
        this.ipaddresstlv = ipaddresstlv;
        return this;
    }

    /**
     * Get the TLV field list.
     *
     * @return the IPCP TLV field list
     */
    public List<PppTlv> getIpcpTlvList() {
        return this.pppTlvList;
    }

    @Override
    public byte[] serialize() {
        // TODO: Can it have any payload?
        final byte[] data = new byte[this.length];
        final ByteBuffer bb = ByteBuffer.wrap(data);
        bb.put(this.code);
        bb.put(this.identifier);
        bb.putShort(this.length);
        if (ipaddresstlv != null) {
            bb.put(ipaddresstlv.serialize());
        }
        if (this.pppTlvList != null) {
            for (final PppTlv tlv : this.pppTlvList) {
                bb.put(tlv.serialize());
            }
        }
        return data;
    }

    /**
     * Deserializer function for IPCP packets.
     *
     * @return deserializer function
     */
    public static Deserializer<Ipcp> deserializer() {
        return (data, offset, length) -> {
            checkInput(data, offset, length, MIN_HEADER_LENGTH);
            Ipcp ipcp = new Ipcp();
            ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
            ipcp.pppProtocol = PppProtocolType.lookup(bb.getShort());
            ipcp.code = bb.get();
            ipcp.identifier = bb.get();
            ipcp.length = bb.getShort();
            int currentIndex = MIN_HEADER_LENGTH;
            PppTlv tlv;
            while (currentIndex < length) {
                // Each new TLV IPCP must be a minimum of 2 bytes
                // (containing the type and length fields).
                currentIndex += 2;
                checkHeaderLength(length, currentIndex);
                tlv = new PppTlv().deserialize(bb);
                // if there was a failure to deserialize stop processing TLVs
                if (tlv == null) {
                    break;
                }
                switch (tlv.getType()) {
                    case PppTlv.IPCPTLV_IP_ADDRESS:
                        // Service Name
                        ipcp.ipaddresstlv = tlv;
                        break;
                    default:
                        ipcp.pppTlvList.add(tlv);
                        break;
                }
                currentIndex += tlv.getLength() - 2;
            }
            if (currentIndex != ipcp.length) {
                throw new DeserializationException("Error in parsing the packet!");
            }
            // TODO: any other upper layer protocol as payload?
            return ipcp;
        };
    }

//    /**
//     * Check if the payload of the PPPoE packet passed is IPCP.
//     *
//     * @param pppoe Packet to check
//     * @return True if the packet contains IPCP header
//     */
//    public static boolean isIpcp(Pppoe pppoe) {
//        return pppoe.getPppProtocol() == Pppoe.PppProtocolType.IPCP.code();
//    }

//    /**
//     * Check if the current instance contain an IP address TLV field.
//     *
//     * @return True if the packet contains IP address TLV field
//     */
//    public boolean haveIpAddressTlv() {
//        return !getIpAddress().isZero() ? true : false;
//    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(ipaddresstlv);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final Ipcp other = (Ipcp) obj;
        return Objects.equal(this.ipaddresstlv, other.ipaddresstlv);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("pppProtocol", pppProtocol)
                .add("code", code)
                .add("identifier", identifier)
                .add("length", length)
                .add("pppTlvList", pppTlvList)
                .add("ipaddresstlv", ipaddresstlv)
                .toString();
    }
}
