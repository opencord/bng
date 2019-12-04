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
import com.google.common.collect.Lists;
import org.onlab.packet.BasePacket;
import org.onlab.packet.Deserializer;
import org.onlab.packet.IPacket;

import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Implements a generic PPP header.
 */

public abstract class Ppp extends BasePacket {

    static final int MIN_HEADER_LENGTH = 4;

    PppProtocolType pppProtocol;

    byte code;
    byte identifier;
    short length; // Length includes the code, identifier, length and data fields

    List<PppTlv> pppTlvList;

    Ppp() {
        super();
        pppTlvList = Lists.newLinkedList();
    }

    /**
     * Get the LCP code.
     *
     * @return the LCP code
     */
    public byte getCode() {
        return code;
    }

    /**
     * Deserializer function for LCP packets.
     *
     * @return deserializer function
     */
//    public static Deserializer<Ppp> deserializer();
////    {
//        return (data, offset, length) -> {
//            checkInput(data, offset, length, MIN_HEADER_LENGTH);
//            Ppp lcp = new Ppp();
//            final ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
//            lcp.code = bb.get();
//            lcp.identifier = bb.get();
//            lcp.length = bb.getShort();
//            int currentIndex = MIN_HEADER_LENGTH;
//            PppTlv tlv;
//            while (currentIndex < length) {
//                // Each new TLV LCP must be a minimum of 2 bytes
//                // (containing the type and length fields).
//                currentIndex += 2;
//                checkHeaderLength(length, currentIndex);
//                tlv = new PppTlv().deserialize(bb);
//                // if there was a failure to deserialize stop processing TLVs
//                if (tlv == null) {
//                    break;
//                }
//                lcp.lcpTlvList.add(tlv);
//                currentIndex += tlv.getLength() - 2;
//            }
//            if (currentIndex != lcp.length) {
//                throw new DeserializationException("Error in parsing the packet!");
//            }
//            // TODO: any other upper layer protocol as payload?
//            return lcp;
//        };
//    }

    /**
     * Check if the passed PPPoE contains as payload LCP header.
     *
     * @param pppoe Packet to check
     * @return True if the packet contains LCP header, False otherwise
     */
//    public static boolean isLcp(Pppoe pppoe) {
//        return pppoe.getPppProtocol() == Pppoe.PppProtocolType.LCP.code();
//    }

//    @Override
//    public byte[] serialize()
//    {
//        byte[] payloadData = null;
//        int payloadLength = 0;
//        if (this.payload != null) {
//            this.payload.setParent(this);
//            payloadData = this.payload.serialize();
//            payloadLength = payloadData.length + MIN_HEADER_LENGTH;
//        }
//        int realLength = this.length > payloadLength ? this.length : payloadLength;
//        final byte[] data = new byte[realLength];
//        final ByteBuffer bb = ByteBuffer.wrap(data);
//        bb.put(this.code);
//        bb.put(this.identifier);
//        bb.putShort(this.length);
//        if (this.lcpTlvList != null) {
//            for (final PppTlv tlv : this.lcpTlvList) {
//                bb.put(tlv.serialize());
//            }
//        }
//        return data;
//    }

    /**
     * Set the LCP code.
     *
     * @param code the LCP code to set
     */
    public void setCode(byte code) {
        this.code = code;
    }

    /**
     * Get the LCP identifier.
     *
     * @return the LCP identifier
     */
    public byte getIdentifier() {
        return identifier;
    }

    /**
     * Set the LCP identifier.
     *
     * @param identifier the LCP identifier to set
     */
    public void setIdentifier(byte identifier) {
        this.identifier = identifier;
    }

    /**
     * Get the length.
     *
     * @return the length
     */
    public short getLength() {
        return length;
    }

    /**
     * Set the length.
     *
     * @param length the length to set
     */
    public void setLength(short length) {
        this.length = length;
    }

    /**
     * Get the LCP tlv options.
     *
     * @return The LCP tlv options
     */
    public List<PppTlv> getPppTlvList() {
        return pppTlvList;
    }

    @Override
    public String toString() {
        return toStringHelper(getClass())
                .add("PPPType", pppProtocol)
                .add("code", code)
                .add("identifier", identifier)
                .add("length", length)
                .add("pppTlvList", pppTlvList)
                .toString();
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(code, identifier, length, pppTlvList);
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
        final Ppp other = (Ppp) obj;
        return Objects.equal(this.code, other.code)
                && Objects.equal(this.identifier, other.identifier)
                && Objects.equal(this.length, other.length)
                && Objects.equal(this.pppTlvList, other.pppTlvList);
    }

//    /**
//     * List of known PPP protocols types.
//     */
//    public enum PppProtocolType {
//        LCP(0xc021, "lcp", true, Lcp.deserializer()),
//        IPCP(0x8021, "ipcp", true, Ipcp.deserializer()),
//        PAP(0xc023, "pap", true, Lcp.deserializer()),
//        CHAP(0xc223, "chap", true, Lcp.deserializer()),
//        IPv4(0x0021, "ipv4", false, org.onlab.packet.IPv4.deserializer()),
//        IPv6(0x0057, "ipv6", false, org.onlab.packet.IPv6.deserializer()),
//        NO_PROTOCOL(0, "no_proto", true, null);
//
//        private final short code;
//        private final String type;
//        private final boolean control;
//        private final Deserializer<? extends IPacket> deserializer;
//
//        /**
//         * Constructs new PPP Protocol types.
//         *
//         * @param code         The PPP Protocol type.
//         * @param type         Textual representation of the PPP Protocol type.
//         * @param control      True if is control plane packet, False
//         *                     otherwise.
//         * @param deserializer The deserialized for the PPP Protocol type.
//         */
//        PppProtocolType(int code, String type, boolean control, Deserializer<? extends IPacket> deserializer) {
//            this.code = (short) (code & 0xFFFF);
//            this.type = type;
//            this.control = control;
//            this.deserializer = deserializer;
//        }
//
//        public static PppProtocolType lookup(short code) {
//            for (PppProtocolType type : PppProtocolType.values()) {
//                if (code == type.code()) {
//                    return type;
//                }
//            }
//            return NO_PROTOCOL;
//        }
//
//        public short code() {
//            return this.code;
//        }
//
//        public String type() {
//            return this.type;
//        }
//
//        public boolean control() {
//            return this.control;
//        }
//
//        public Deserializer<? extends IPacket> deserializer() {
//            return this.deserializer;
//        }
//    }
}
