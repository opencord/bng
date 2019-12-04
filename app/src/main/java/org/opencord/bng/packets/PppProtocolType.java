package org.opencord.bng.packets;

import org.onlab.packet.Deserializer;
import org.onlab.packet.IPacket;

public enum PppProtocolType {
    LCP(0xc021, "lcp", true, Lcp.deserializer()),
    IPCP(0x8021, "ipcp", true, Ipcp.deserializer()),
    PAP(0xc023, "pap", true, Lcp.deserializer()),
    CHAP(0xc223, "chap", true, Lcp.deserializer()),
    IPv4(0x0021, "ipv4", false, org.onlab.packet.IPv4.deserializer()),
    IPv6(0x0057, "ipv6", false, org.onlab.packet.IPv6.deserializer()),
    NO_PROTOCOL(0, "no_proto", true, null);

    private final short code;
    private final String type;
    private final boolean control;
    private final Deserializer<? extends IPacket> deserializer;

    /**
     * Constructs new PPP Protocol types.
     *
     * @param code         The PPP Protocol type.
     * @param type         Textual representation of the PPP Protocol type.
     * @param control      True if is control plane packet, False otherwise.
     * @param deserializer The deserialized for the PPP Protocol type.
     */
    PppProtocolType(int code, String type, boolean control, Deserializer<? extends IPacket> deserializer) {
        this.code = (short) (code & 0xFFFF);
        this.type = type;
        this.control = control;
        this.deserializer = deserializer;
    }

    /**
     * Lookups for a PPP Protocol type
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

    /**
     * Returns the deserialized associated to the PPP protocol.
     *
     * @return The PPP protocol deserializer
     */
    public Deserializer<? extends IPacket> deserializer() {
        return this.deserializer;
    }
}
