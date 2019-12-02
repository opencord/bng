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

package org.opencord.bng.parser;


import com.google.common.base.Objects;
import org.onlab.packet.BasePacket;
import org.onlab.packet.Data;
import org.onlab.packet.Deserializer;

import java.nio.ByteBuffer;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.onlab.packet.PacketUtils.checkInput;

/**
 * Implements the Link Control Protocol (LCP) header.
 */
public class Lcp extends BasePacket {

    //TODO: support more in LCP packet options (e.g., options, magic number...)

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

    protected byte code;
    protected byte identifier;
    protected short length;

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
        if (payloadData != null) {
            bb.put(payloadData);
        }
        return data;
    }

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
            lcp.code = bb.get();
            lcp.identifier = bb.get();
            lcp.length = bb.getShort();
            if (lcp.length > MIN_HEADER_LENGTH) {
                lcp.payload = Data.deserializer().deserialize(data, bb.position(),
                                                              bb.limit() - bb.position());
                lcp.payload.setParent(lcp);
            }
            return lcp;
        };
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

    @Override
    public String toString() {
        return toStringHelper(getClass())
                .add("code", code)
                .add("identifier", identifier)
                .add("length", length)
                .toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Ipcp)) {
            return false;
        }
        final Ipcp other = (Ipcp) obj;
        if (this.code != other.code) {
            return false;
        }
        if (this.identifier != other.identifier) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), code, identifier, length);
    }

    /**
     * Check if the passed PPPoE contains as payload LCP header.
     *
     * @param pppoe Packet to check
     * @return True if the packet contains LCP header, False otherwise
     */
    public static boolean isLcp(Pppoe pppoe) {
        return pppoe.getPppProtocol() == Pppoe.PppProtocolType.LCP.code();
    }
}
