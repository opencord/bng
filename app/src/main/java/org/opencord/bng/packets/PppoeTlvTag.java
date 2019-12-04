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
 * Implements the PPPoE Type-Length-Value TAGs.
 */
public class PppoeTlvTag {

    public static final short PPPOED_TAGTYPE_EOL = 0;
    public static final short PPPOED_TAGTYPE_SERVICENAME = 0x0101;
    public static final short PPPOED_TAGTYPE_ACNAME = 0x0102;

    private short tagType;
    private short length; // Length excluded the header (4 bytes minimum header)
    private byte[] value;

    /**
     * Gets the TLV TAG type.
     *
     * @return the TLV TAG type
     */
    public short getTagType() {
        return tagType;
    }

    /**
     * Sets the TLV TAG type.
     *
     * @param tagType the type to set
     * @return this
     */
    public PppoeTlvTag setTagType(short tagType) {
        this.tagType = tagType;
        return this;
    }

    /**
     * Gets the length, number of bytes of value field.
     *
     * @return the length
     */
    public short getLength() {
        return length;
    }

    /**
     * Sets the length.
     *
     * @param length the length to set excluded the header length
     * @return this
     */
    public PppoeTlvTag setLength(final short length) {
        this.length = length;
        return this;
    }

    /**
     * The TLV value.
     *
     * @return the value
     */
    public byte[] getValue() {
        return this.value;
    }

    /**
     * Set the TLV value.
     *
     * @param value the value to set
     * @return this
     */
    public PppoeTlvTag setValue(final byte[] value) {
        this.value = value;
        return this;
    }

    public byte[] serialize() {
        final byte[] data = new byte[4 + this.length];
        final ByteBuffer bb = ByteBuffer.wrap(data);
        bb.putShort(tagType);
        bb.putShort(length);
        if (this.value != null) {
            bb.put(this.value);
        }
        return data;
    }

    public PppoeTlvTag deserialize(final ByteBuffer bb) throws DeserializationException {
        if (bb.remaining() < 4) {
            throw new DeserializationException(
                    "Not enough bytes to deserialize PPPoE TLV tag type and length");
        }
        this.tagType = bb.getShort();
        this.length = bb.getShort();

        if (this.length > 0) {
            this.value = new byte[this.length];

            // if there is an underrun just toss the TLV
            if (bb.remaining() < this.length) {
                throw new DeserializationException(
                        "Remaining bytes are less then the length of the PPPoE TLV tag");
            }
            bb.get(this.value);
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("type= ");
        sb.append(this.tagType);
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
        if (!(obj instanceof PppoeTlvTag)) {
            return false;
        }
        final PppoeTlvTag other = (PppoeTlvTag) obj;
        if (this.length != other.length) {
            return false;
        }
        if (this.tagType != other.tagType) {
            return false;
        }
        if (!Arrays.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tagType, length, Arrays.hashCode(value));
    }
}
