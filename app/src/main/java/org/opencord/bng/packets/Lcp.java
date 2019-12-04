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

import java.nio.ByteBuffer;
import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.onlab.packet.PacketUtils.checkHeaderLength;
import static org.onlab.packet.PacketUtils.checkInput;

/**
 * Implements the Link Control Protocol (LCP) header.
 */

public class Lcp extends Ppp {

    // TODO: Add support for TLV options.

    public static final byte CODE_TERM_REQ = 0x05;
    public static final byte CODE_TERM_ACK = 0x06;

    public static final byte CHAP_CODE_CHALLENGE = 0x01;
    public static final byte CHAP_CODE_RESPONSE = 0x02;
    public static final byte CHAP_CODE_SUCCESS = 0x03;
    public static final byte CHAP_CODE_FAILURE = 0x04;

    public static final byte PAP_AUTH_REQ = 0x01;
    public static final byte PAP_AUTH_ACK = 0x02;
    public static final byte PAP_AUTH_NACK = 0x03;

    static final int MIN_HEADER_LENGTH = 4;

//    protected byte code;
//    protected byte identifier;
//    protected short length; // Length includes the code, identifier, length and data fields


//    protected List<PppTlv> lcpTlvList;
//
//    public Lcp() {
//        super();
//        lcpTlvList = Lists.newLinkedList();
//    }

    /**
     * Deserializer function for LCP packets.
     *
     * @return deserializer function
     */
    public static Deserializer<Lcp> deserializer() {
        return (data, offset, length) -> {
            checkInput(data, offset, length, MIN_HEADER_LENGTH);
            Lcp lcp = new Lcp();
            final ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
            lcp.pppProtocol = PppProtocolType.lookup(bb.getShort());
            lcp.code = bb.get();
            lcp.identifier = bb.get();
            lcp.length = bb.getShort();
            int currentIndex = MIN_HEADER_LENGTH;
            PppTlv tlv;
            while (currentIndex < length) {
                // Each new TLV LCP must be a minimum of 2 bytes
                // (containing the type and length fields).
                currentIndex += 2;
                checkHeaderLength(length, currentIndex);
                tlv = new PppTlv().deserialize(bb);
                // if there was a failure to deserialize stop processing TLVs
                if (tlv == null) {
                    break;
                }
                lcp.pppTlvList.add(tlv);
                currentIndex += tlv.getLength() - 2;
            }
            if (currentIndex != lcp.length) {
                throw new DeserializationException("Error in parsing the packet!");
            }
            // TODO: any other upper layer protocol as payload?
            return lcp;
        };
    }

//    /**
//     * Check if the passed PPPoE contains as payload LCP header.
//     *
//     * @param pppoe Packet to check
//     * @return True if the packet contains LCP header, False otherwise
//     */
//    public static boolean isLcp(Pppoe pppoe) {
//        return pppoe.getPppProtocol() == Ppp.PppProtocolType.LCP.code();
//    }

    @Override
    public byte[] serialize() {
        byte[] payloadData = null;
        int payloadLength = 0;
        if (this.payload != null) {
            this.payload.setParent(this);
            payloadData = this.payload.serialize();
            payloadLength = payloadData.length + MIN_HEADER_LENGTH;
        }
        int realLength = this.length > payloadLength ? this.length : payloadLength;
        final byte[] data = new byte[realLength];
        final ByteBuffer bb = ByteBuffer.wrap(data);
        bb.put(this.code);
        bb.put(this.identifier);
        bb.putShort(this.length);
        if (this.pppTlvList != null) {
            for (final PppTlv tlv : this.pppTlvList) {
                bb.put(tlv.serialize());
            }
        }
        return data;
    }

    /**
     * Get the LCP tlv options.
     *
     * @return The LCP tlv options
     */
    public List<PppTlv> getLcpTlvList() {
        return this.getPppTlvList();
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
        final Lcp other = (Lcp) obj;
        return Objects.equal(this.code, other.code)
                && Objects.equal(this.identifier, other.identifier)
                && Objects.equal(this.length, other.length)
                && Objects.equal(this.pppTlvList, other.pppTlvList);
    }
}
