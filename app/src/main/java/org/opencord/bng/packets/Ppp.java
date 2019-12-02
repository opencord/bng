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
import org.onlab.packet.BasePacket;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Implements a generic PPP header.
 */

public abstract class Ppp extends BasePacket {

    static final int MIN_HEADER_LENGTH = 4;

    byte code;
    byte identifier;
    short length; // Length includes the code, identifier, length and data fields

    /**
     * Gets the LCP code.
     *
     * @return the LCP code
     */
    public byte getCode() {
        return code;
    }

    /**
     * Sets the LCP code.
     *
     * @param code the LCP code to set
     */
    public void setCode(byte code) {
        this.code = code;
    }

    /**
     * Gets the LCP identifier.
     *
     * @return the LCP identifier
     */
    public byte getIdentifier() {
        return identifier;
    }

    /**
     * Sets the LCP identifier.
     *
     * @param identifier the LCP identifier to set
     */
    public void setIdentifier(byte identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    public short getLength() {
        return length;
    }

    /**
     * Sets the length.
     *
     * @param length the length to set
     */
    public void setLength(short length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return toStringHelper(getClass())
//                .add("PPPType", pppProtocol)
                .add("code", code)
                .add("identifier", identifier)
                .add("length", length)
                .toString();
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(code, identifier, length);
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
                && Objects.equal(this.length, other.length);
    }
}
