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

/**
 * PPP Protocol type enumerator.
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
     * @param code         The PPP Protocol type.
     * @param type         Textual representation of the PPP Protocol type.
     * @param control      True if is control plane packet, false otherwise.
     */
    PppProtocolType(int code, String type, boolean control) {
        this.code = (short) (code & 0xFFFF);
        this.type = type;
        this.control = control;
    }

    /**
     * Lookups for a PPP Protocol type.
     *
     * @param code The code for PPP protocol.
     * @return The PPPProtocol type
     */
    public static PppProtocolType lookup(short code) {
        for (PppProtocolType type : PppProtocolType.values()) {
            if (code == type.code()) {
                return type;
            }
        }
        return NO_PROTOCOL;
    }

    /**
     * Returns code associated to the PPP protocol.
     *
     * @return The code for PPP protocol
     */
    public short code() {
        return this.code;
    }

    /**
     * Returns the string representation of the PPP protocol.
     *
     * @return The PPP protocol string representation
     */
    public String type() {
        return this.type;
    }

    /**
     * Checks if the PPP protocol is carrying control plane packets.
     *
     * @return True if the PPP protocol is for control plane packets, false
     * otherwise
     */
    public boolean control() {
        return this.control;
    }
}
