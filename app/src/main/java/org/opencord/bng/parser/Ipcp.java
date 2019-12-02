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
import org.onlab.packet.Deserializer;
import org.onlab.packet.IpAddress;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.onlab.packet.PacketUtils.checkHeaderLength;
import static org.onlab.packet.PacketUtils.checkInput;

/**
 * Implements the IP Control Protocol (IPCP) packet format.
 */
public class Ipcp extends BasePacket {

    static final int MIN_HEADER_LENGTH = 4;

    public static final byte CONF_REQ = 0x01;
    public static final byte ACK = 0x02;
    public static final byte NAK = 0x03;

    protected byte code;
    protected byte identifier;
    protected short length; // Payload + header length

    protected IpcpTlv ipaddresstlv = null;

    protected List<IpcpTlv> ipcpTlvList;

    public Ipcp() {
        super();
        ipcpTlvList = new LinkedList<>();
    }

    /**
     * Get the IP Address TLV field.
     *
     * @return the IP Address TLV field if present, null otherwise
     */
    public IpcpTlv getIpAddressTlv() {
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
    public Ipcp setIpAddressTlv(IpcpTlv ipaddresstlv) {
        this.ipaddresstlv = ipaddresstlv;
        return this;
    }

    /**
     * Get the TLV field list.
     *
     * @return the IPCP TLV field list
     */
    public List<IpcpTlv> getIpcpTlvList() {
        return this.ipcpTlvList;
    }

    /**
     * Get the IPCP code.
     *
     * @return the IPCP code
     */
    public byte getCode() {
        return this.code;
    }

    /**
     * Get the IPCP length.
     *
     * @return the IPCP length
     */
    public short getLength() {
        return this.length;
    }

    /**
     * Get the IPCP identifier.
     *
     * @return the IPCP identifier
     */
    public short getIdentifier() {
        return this.identifier;
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
        if (this.ipcpTlvList != null) {
            for (final IpcpTlv tlv : this.ipcpTlvList) {
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
            ipcp.code = bb.get();
            ipcp.identifier = bb.get();
            ipcp.length = bb.getShort();
            int currentIndex = MIN_HEADER_LENGTH;
            IpcpTlv tlv;
            while (currentIndex < length) {
                // Each new TLV IPCP must be a minimum of 2 bytes
                // (containing the type and length fields).
                currentIndex += 2;
                checkHeaderLength(length, currentIndex);
                tlv = new IpcpTlv().deserialize(bb);
                // if there was a failure to deserialize stop processing TLVs
                if (tlv == null) {
                    break;
                }
                switch (tlv.getType()) {
                    case IpcpTlv.IPCPTLV_IP_ADDRESS:
                        // Service Name
                        ipcp.ipaddresstlv = tlv;
                        break;
                    default:
                        ipcp.ipcpTlvList.add(tlv);
                        break;
                }
                currentIndex += tlv.getLength();
            }
            // TODO: any other upper layer protocol as payload?
            return ipcp;
        };
    }

    @Override
    public String toString() {
        return toStringHelper(getClass())
                .add("code", code)
                .add("identifier", identifier)
                .add("length", length)
                .add("ipaddresstlv", ipaddresstlv)
                .add("ipcptlvList", ipcpTlvList)
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
        if (this.ipaddresstlv != other.ipaddresstlv) {
            return false;
        }
        if (!this.ipcpTlvList.equals(other.ipcpTlvList)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), code, identifier, length, ipaddresstlv, ipcpTlvList);
    }

    /**
     * Check if the payload of the PPPoE packet passed is IPCP.
     *
     * @param pppoe Packet to check
     * @return True if the packet contains IPCP header
     */
    public static boolean isIpcp(Pppoe pppoe) {
        return pppoe.getPppProtocol() == Pppoe.PppProtocolType.IPCP.code();
    }

    /**
     * Check if the current instance contain an IP address TLV field.
     *
     * @return True if the packet contains IP address TLV field
     */
    public boolean haveIpAddressTlv() {
        return !getIpAddress().isZero() ? true : false;
    }
}
