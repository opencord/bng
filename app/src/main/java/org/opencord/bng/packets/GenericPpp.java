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

//    /**
//     * Deserializer function for LCP packets.
//     *
//     * @return deserializer function
//     */
//    public static Deserializer<Lcp> deserializer() {
//        return (data, offset, length) -> {
//            checkInput(data, offset, length, MIN_HEADER_LENGTH);
//            Lcp lcp = new Lcp();
//            final ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
//            lcp.code = bb.get();
//            lcp.identifier = bb.get();
//            lcp.length = bb.getShort();
//            short currentIndex = MIN_HEADER_LENGTH;
//            PppTlv tlv;
//            while (currentIndex < lcp.length) {
//                // Each new TLV LCP must be a minimum of 2 bytes
//                // (containing the type and length fields).
//                currentIndex += 2;
//                checkHeaderLength(length, currentIndex);
//                try {
//                    tlv = new PppTlv().deserialize(bb);
//                } catch(DeserializationException e) {
//                    throw new DeserializationException(e.getMessage() + lcp.toString());
//                }
//                // if there was a failure to deserialize stop processing TLVs
//                if (tlv == null) {
//                    break;
//                }
//                lcp.pppTlvList.add(tlv);
//                currentIndex += tlv.getLength() - 2;
//            }
//            if (currentIndex != lcp.length) {
//                throw new DeserializationException("Error in parsing the packet!" + lcp.toString() + "\n" +
//                                                           "Length: " + length + "\n"+
//                                                           "Lcp.length: " + lcp.length + "\n"+
//                                                           "currentIndex: " + currentIndex + "\n");
//            }
//            // TODO: any other upper layer protocol as payload?
//            return lcp;
//        };
//    }

//    /**
//     * Check if the passed PPPoE contains as payload LCP header.
//     *
//     * @param pppoe Packet to check
//     * @return True if the packet contains LCP header, False otherwise
//     */
//    public static boolean isLcp(Pppoe pppoe) {
//        return pppoe.getPppProtocol() == Ppp.PppProtocolType.LCP.code();
//    }

//    @Override
//    public byte[] serialize() {
//        byte[] payloadData = null;
//        int payloadLength = 0;
//        if (this.payload != null) {
//            this.payload.setParent(this);
//            payloadData = this.payload.serialize();
//            payloadLength = payloadData.length + MIN_HEADER_LENGTH;
//        }
//        int realLength = this.length > payloadLength ? this.length : payloadLength;
//        final byte[] data = new byte[realLength];
//        final ByteBuffer bb = ByteBuffer.wrap(data);
//        bb.put(this.code);
//        bb.put(this.identifier);
//        bb.putShort(this.length);
//        if (this.pppTlvList != null) {
//            for (final PppTlv tlv : this.pppTlvList) {
//                bb.put(tlv.serialize());
//            }
//        }
//        return data;
//    }

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

