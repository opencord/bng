/*
 * Copyright 2019-2023 Open Networking Foundation (ONF) and the ONF Contributors
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
import org.onlab.packet.Data;
import org.onlab.packet.Deserializer;

import java.nio.ByteBuffer;

import static org.onlab.packet.PacketUtils.checkInput;

/**
 * Implements the Link Control Protocol (LCP) header.
 */

public class GenericPpp extends Ppp {

    // TODO: Add support for TLV options.

    public static final byte CHAP_CODE_CHALLENGE = 0x01;
    public static final byte CHAP_CODE_RESPONSE = 0x02;
    public static final byte CHAP_CODE_SUCCESS = 0x03;
    public static final byte CHAP_CODE_FAILURE = 0x04;

    public static final byte PAP_AUTH_REQ = 0x01;
    public static final byte PAP_AUTH_ACK = 0x02;
    public static final byte PAP_AUTH_NACK = 0x03;

    public static final byte CODE_TERM_REQ = 0x05;
    public static final byte CODE_TERM_ACK = 0x06;


    /**
     * Deserializer function for LCP packets.
     *
     * @return deserializer function
     */
    public static Deserializer<GenericPpp> deserializer() {
        return (data, offset, length) -> {
            checkInput(data, offset, length, MIN_HEADER_LENGTH);
            GenericPpp ppp = new GenericPpp();
            final ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
            ppp.code = bb.get();
            ppp.identifier = bb.get();
            ppp.length = bb.getShort();
            if (ppp.length > MIN_HEADER_LENGTH) {
                ppp.payload = Data.deserializer()
                        .deserialize(data, bb.position(),
                                     Math.min(bb.limit() - bb.position(),
                                              ppp.length - MIN_HEADER_LENGTH));
                ppp.payload.setParent(ppp);
            }
            return ppp;
        };
    }

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
        final GenericPpp other = (GenericPpp) obj;
        return Objects.equal(this.code, other.code)
                && Objects.equal(this.identifier, other.identifier)
                && Objects.equal(this.length, other.length);
    }
}

