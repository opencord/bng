package org.opencord.bng;

import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.net.ConnectPoint;

public final class BngUtils {
    /**
     * Extract the BNG attachment ID given an event subject.
     *
     * @param eventInfo The event subsject
     * @return BNG attachment ID
     */
    public static String calcBngAttachmentId(PppoeEventSubject eventInfo) {
        return calcBngAttachmentId(eventInfo.getOnuSerialNumber(),
                                   eventInfo.getcTag(), eventInfo.getsTag(),
                                   eventInfo.getOltConnectPoint(), eventInfo.getIpAddress(),
                                   eventInfo.getMacAddress());
    }

    /**
     * Extract the BNG attachment ID given some of the attachment fields.
     *
     * @param onuSerialNumber Serial number of the ONU
     * @param cTag            VLAN C-Tag
     * @param sTag            VLAN S-Tag
     * @param oltConnectPoint The OLT-level connect point
     * @param ipAddress       The attachment IP address
     * @param macAddress      The attachment MAC addres
     * @return The built attachment ID
     */
    public static String calcBngAttachmentId(String onuSerialNumber,
                                             VlanId cTag, VlanId sTag,
                                             ConnectPoint oltConnectPoint,
                                             IpAddress ipAddress,
                                             MacAddress macAddress) {
        return String.join("/", onuSerialNumber, cTag.toString(),
                           sTag.toString(), oltConnectPoint.toString(),
                           ipAddress.toString(), macAddress.toString()
        );
    }
}
