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

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Implements IPCP Type-Length-Value options.
 */
public class PppTlv {
    public static final byte IPCPTLV_IP_ADDRESS = 0x03;
    private byte type;
    private byte length; // Including the 2 byte of Minimum header
    private byte[] value;

    /**
     * Get the TLV type.
     *
     * @return the TLV type
     */
    public byte getType() {
        return type;
    }

    /**
     * Set the TLV type.
     *
     * @param type the TLV type to set
     * @return this
     */
    public PppTlv setType(byte type) {
        this.type = type;
        return this;
    }

    /**
     * Get the TLV length. The length include the 2 bytes of minimum header
     * length.
     *
     * @return the TLV length
     */
    public byte getLength() {
        return length;
    }

    /**
     * Set the TLV length. Length must include the 2 bytes of minimum header
     * length.
     *
     * @param length the TLV length to set.
     * @return this
     */
    public PppTlv setLength(byte length) {
        this.length = length;
        return this;
    }

    /**
     * Get the TLV value field as byte array.
     *
     * @return the TLV value field
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * Set the TLV valued field.
     *
     * @param value the TLV value to set
     * @return this
     */
    public PppTlv setValue(byte[] value) {
        this.value = value;
        return this;
    }

    public byte[] serialize() {
        final byte[] data = new byte[this.length];
        final ByteBuffer bb = ByteBuffer.wrap(data);
        bb.put(type);
        bb.put(length);
        if (this.value != null) {
            bb.put(this.value);
        }
        return data;
    }

    public PppTlv deserialize(final ByteBuffer bb) throws DeserializationException {
        if (bb.remaining() < 2) {
            throw new DeserializationException(
                    "Not enough bytes to deserialize PPP TLV options");
        }
        this.type = bb.get();
        this.length = bb.get();
        if (this.length > 2) {
            this.value = new byte[this.length - 2];

            // if there is an underrun just toss the TLV
            // Length include the length of the TLV header itself
            if (bb.remaining() < this.length - 2) {
                throw new DeserializationException(
                        "Remaining bytes are less then the length of the PPP TLV tag");
            }
            bb.get(this.value);
            return this;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("type= ");
        sb.append(this.type);
        sb.append("length= ");
        sb.append(this.length);
        sb.append("value= ");
        sb.append(Arrays.toString(this.value));
        sb.append("]");
        return sb.toString();
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
        if (!(obj instanceof PppTlv)) {
            return false;
        }
        final PppTlv other = (PppTlv) obj;
        if (this.length != other.length) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Arrays.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, length, Arrays.hashCode(value));
    }
}
