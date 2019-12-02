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
import com.google.common.collect.ImmutableMap;
import org.onlab.packet.BasePacket;
import org.onlab.packet.Data;
import org.onlab.packet.Deserializer;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPacket;
import org.onlab.packet.IPv4;
import org.onlab.packet.IPv6;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.onlab.packet.PacketUtils.checkHeaderLength;
import static org.onlab.packet.PacketUtils.checkInput;

/**
 * Implements PPPoE packet format.
 */
public class Pppoe extends BasePacket {

    public static final short TYPE_PPPOED = (short) 0x8863;
    public static final short TYPE_PPPOES = (short) 0x8864;

    /**
     * List of known PPPoE Discovery types.
     */
    public enum PppoeType {
        PADI(0x9, "padi"),
        PADO(0x7, "pado"),
        PADR(0x19, "padr"),
        PADS(0x65, "pads"),
        PADT(0xa7, "padt"),
        SESSION(0x0, "session"),
        UNKNOWN(0xFF, "unknown");

        private final byte code;
        private final String type;

        /**
         * Constructs new PPPoE Discovery types.
         *
         * @param code The PPPoED type.
         * @param type Textual representation of the PPPoED type.
         */
        PppoeType(int code, String type) {
            this.code = (byte) (code & 0xFF);
            this.type = type;
        }

        public byte code() {
            return code;
        }

        public String type() {
            return type;
        }

        public static PppoeType lookup(byte code) {
            for (PppoeType type : PppoeType.values()) {
                if (code == type.code()) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * List of known PPP protocols types.
     */
    public enum PppProtocolType {
        LCP(0xc021, "lcp", true),
        IPCP(0x8021, "ipcp", true),
        PAP(0xc023, "pap", true),
        CHAP(0xc223, "chap", true),
        IPv4(0x0021, "ipv4", false),
        IPv6(0x0057, "ipv6", false),
        NO_PROTOCOL(0, "no_proto", true);

        private final short code;
        private final String type;
        private final boolean control;

        /**
         * Constructs new PPP Protocol types.
         *
         * @param code    The PPP Protocol type.
         * @param type    Textual representation of the PPP Protocol type.
         * @param control True if is control plane packet, False otherwise.
         */
        PppProtocolType(int code, String type, boolean control) {
            this.code = (short) (code & 0xFFFF);
            this.type = type;
            this.control = control;
        }

        public short code() {
            return this.code;
        }

        public String type() {
            return this.type;
        }

        public boolean control() {
            return this.control;
        }

        public static PppProtocolType lookup(short code) {
            for (PppProtocolType type : PppProtocolType.values()) {
                if (code == type.code()) {
                    return type;
                }
            }
            return NO_PROTOCOL;
        }
    }

    public static final Map<Short, Deserializer<? extends IPacket>> PROTOCOL_DESERIALIZER_MAP =
            ImmutableMap.<Short, Deserializer<? extends IPacket>>builder()
                    .put(PppProtocolType.LCP.code(), Lcp.deserializer())
                    //FIXME: write the correct parser for PAP and CHAP payload
                    .put(PppProtocolType.PAP.code(), Lcp.deserializer())
                    .put(PppProtocolType.CHAP.code(), Lcp.deserializer())
                    .put(PppProtocolType.IPCP.code(), Ipcp.deserializer())
                    .put(PppProtocolType.IPv4.code(), IPv4.deserializer())
                    .put(PppProtocolType.IPv6.code(), IPv6.deserializer())
                    .build();

    // Account of PPPoE standard header
    private static final int HEADER_LENGTH = 6;

    // Fields part of PPPoE
    protected byte version; // 4bit
    protected byte typeId;  // 4bit
    protected PppoeType packetType; // 8bit => code in PPPoE header
    protected short sessionId; // 16bit
    protected short payloadLength; // 16bit

    // FIXME: use PPPProtocol enum type
    protected short pppProtocol = 0;

    //TODO: TLV TAGs (ref. to RFC2516)
    protected PppoeTlvTag acName = null;
    protected PppoeTlvTag serviceName = null;
    protected List<PppoeTlvTag> optionalTagTlvList;

    public Pppoe() {
        super();
        optionalTagTlvList = new LinkedList<>();
    }

    /**
     * Get the packet type.
     *
     * @return the packet type
     */
    public PppoeType getPacketType() {
        return packetType;
    }

    /**
     * Get the session ID.
     *
     * @return the session ID
     */
    public short getSessionId() {
        return this.sessionId;
    }

    /**
     * Get the Point-to-Point Protocol.
     *
     * @return the Point-to-Point Protocol
     */
    public short getPppProtocol() {
        return this.pppProtocol;
    }

    /**
     * Get the AC-Name.
     *
     * @return the AC-Name
     */
    public PppoeTlvTag getAcName() {
        return this.acName;
    }

    /**
     * Set the AC-Name.
     *
     * @param acname AC-Name to set
     * @return this
     */
    public Pppoe setAcName(final PppoeTlvTag acname) {
        this.acName = acname;
        return this;
    }

    /**
     * Set the version field.
     *
     * @param version version to set
     * @return this
     */
    public Pppoe setVersion(byte version) {
        this.version = (byte) (version & 0xF);
        return this;
    }

    /**
     * Set the type IF.
     *
     * @param typeId Type ID to set
     * @return this
     */
    public Pppoe setTypeId(byte typeId) {
        this.typeId = (byte) (typeId & 0xF);
        return this;
    }

    /**
     * Set the Packet Type.
     *
     * @param type The packet type to set
     * @return this
     */
    public Pppoe setPacketType(PppoeType type) {
        this.packetType = type;
        return this;
    }

    /**
     * Set the session ID.
     *
     * @param sessionId the session ID to set
     * @return this
     */
    public Pppoe setSessionId(short sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Set the payload length.
     *
     * @param payloadLength the payload length to set.
     * @return this
     */
    public Pppoe setPayloadLength(short payloadLength) {
        this.payloadLength = payloadLength;
        return this;
    }


    @Override
    public byte[] serialize() {
        byte[] payloadData = null;
        int payloadLength = 0;
        if (this.payload != null) {
            this.payload.setParent(this);
            payloadData = this.payload.serialize();
            payloadLength = payloadData.length + HEADER_LENGTH +
                    (this.packetType == PppoeType.SESSION ? 2 : 0);
        }
        // PayloadLength account for PPP header field
        int realLength = Math.max(this.payloadLength + HEADER_LENGTH, payloadLength);
        final byte[] data = new byte[realLength];
        final ByteBuffer bb = ByteBuffer.wrap(data);
        bb.put((byte) (this.version << 4 | this.typeId & 0xf));
        bb.put(this.packetType.code);
        bb.putShort(this.sessionId);
        bb.putShort(this.payloadLength);
        if (this.packetType == PppoeType.SESSION) {
            // Only session packet have PPP header
            bb.putShort(this.pppProtocol);
        } else {
            // Only NON session packet have options
            // TLV Tags
            if (acName != null) {
                bb.put(acName.serialize());
            }
            if (serviceName != null) {
                bb.put(serviceName.serialize());
            }
            if (this.optionalTagTlvList != null) {
                for (final PppoeTlvTag tlv : this.optionalTagTlvList) {
                    bb.put(tlv.serialize());
                }
            }
        }
        if (payloadData != null) {
            bb.put(payloadData);
        }
        return data;
    }

    /**
     * Deserializer function for PPPoE packets.
     *
     * @return deserializer function
     */
    public static Deserializer<Pppoe> deserializer() {
        return (data, offset, length) -> {
            checkInput(data, offset, length, HEADER_LENGTH);

            Pppoe pppoe = new Pppoe();
            final ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
            byte versionAndType = bb.get();
            pppoe.version = (byte) (versionAndType >> 4 & 0xF);
            pppoe.typeId = (byte) (versionAndType & 0xF);
            byte code = bb.get();
            pppoe.sessionId = bb.getShort();
            pppoe.payloadLength = bb.getShort();
            pppoe.packetType = PppoeType.lookup(code);
            // Check if the PPPoE packet is a SESSION packet
            if (pppoe.packetType == PppoeType.SESSION) {
                // Parse inner protocols
                pppoe.pppProtocol = bb.getShort();
                Deserializer<? extends IPacket> deserializer;
                if (Pppoe.PROTOCOL_DESERIALIZER_MAP.containsKey(pppoe.pppProtocol)) {
                    deserializer = PROTOCOL_DESERIALIZER_MAP.get(pppoe.pppProtocol);
                } else {
                    deserializer = Data.deserializer();
                }
                int remainingLength = bb.limit() - bb.position();
                int bytesToRead = (pppoe.payloadLength <= remainingLength) ?
                        pppoe.payloadLength : remainingLength;

                pppoe.payload = deserializer.deserialize(data, bb.position(), bytesToRead);
                pppoe.payload.setParent(pppoe);
            } else {
                // PPPoE Packet is of Discovery type
                // Parse TLV PPPoED Tags
                int currentIndex = HEADER_LENGTH;
                PppoeTlvTag tlv;
                do {
                    // Each new TLV PPPoE TAG must be a minimum of 4 bytes
                    // (containing the type and length fields).
                    if (length - currentIndex < 4) {
                        // Probably the packet was zero-padded to reach the Ethernet minimum length.
                        // Let's skip and accept the packet
                        // FIXME: is there a smarter way to identify a padded packet?
                        break;
                    }
                    currentIndex += 4;
                    checkHeaderLength(length, currentIndex);
                    tlv = new PppoeTlvTag().deserialize(bb);
                    // if there was a failure to deserialize stop processing TLVs
                    if (tlv == null) {
                        break;
                    }
                    switch (tlv.getTagType()) {
                        case PppoeTlvTag.PPPOED_TAGTYPE_EOL:
                            // end delimiter
                            break;
                        case PppoeTlvTag.PPPOED_TAGTYPE_SERVICENAME:
                            // Service Name
                            pppoe.serviceName = tlv;
                            break;
                        case PppoeTlvTag.PPPOED_TAGTYPE_ACNAME:
                            // AC-Name
                            pppoe.acName = tlv;
                            break;
                        default:
                            pppoe.optionalTagTlvList.add(tlv);
                            break;
                    }
                    currentIndex += tlv.getLength();
                } while (tlv.getTagType() != 0 && currentIndex < length);
            }
            return pppoe;
        };
    }

    @Override
    public String toString() {
        return toStringHelper(getClass())
                .add("version", Byte.toString(version))
                .add("typeId", Byte.toString(typeId))
                .add("code", Byte.toString(packetType.code))
                .add("sessionId", Short.toString(sessionId))
                .add("payloadLength", Short.toString(payloadLength))
                .add("PPP.protocol", Short.toString(pppProtocol))
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IpcpTlv)) {
            return false;
        }
        final Pppoe other = (Pppoe) obj;
        if (this.version != other.version) {
            return false;
        }
        if (this.typeId != other.typeId) {
            return false;
        }
        if (this.packetType != other.packetType) {
            return false;
        }
        if (this.sessionId != other.sessionId) {
            return false;
        }
        if (this.payloadLength != other.payloadLength) {
            return false;
        }
        if (this.pppProtocol != other.pppProtocol) {
            return false;
        }
        if (this.acName == null) {
            if (other.acName != null) {
                return false;
            }
        } else if (!this.acName.equals(other.acName)) {
            return false;
        }
        if (this.serviceName == null) {
            if (other.serviceName != null) {
                return false;
            }
        } else if (!this.serviceName.equals(other.serviceName)) {
            return false;
        }
        if (!this.optionalTagTlvList.equals(other.optionalTagTlvList)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), version, typeId, packetType, sessionId,
                                payloadLength, pppProtocol, acName, serviceName, optionalTagTlvList);
    }

    /**
     * Check if the passed Ethernet packet is PPPoE Service.
     *
     * @param eth Packet to check
     * @return True if the packet contains PPPoE Service header
     */
    public static boolean isPPPoES(Ethernet eth) {
        return eth.getEtherType() == TYPE_PPPOES;
    }

    /**
     * Check if the passed Ethernet packet is PPPoE Discovery.
     *
     * @param eth Packet to check
     * @return True if the packet contains PPPoE Discovery header
     */
    public static boolean isPPPoED(Ethernet eth) {
        return eth.getEtherType() == TYPE_PPPOED;
    }
}
