/*
 * Copyright 2019-2024 Open Networking Foundation (ONF) and the ONF Contributors
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

package org.opencord.bng.impl;

import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.net.ConnectPoint;
import org.opencord.bng.PppoeEventSubject;

public final class BngUtils {

    private BngUtils() {

    }

    /**
     * Extract the BNG attachment key given an event subject.
     *
     * @param eventInfo The event subsject
     * @return BNG attachment ID
     */
    public static String calculateBngAttachmentKey(PppoeEventSubject eventInfo) {
        return calculateBngAttachmentKey(eventInfo.getOnuSerialNumber(),
                                         eventInfo.getcTag(), eventInfo.getsTag(),
                                         eventInfo.getOltConnectPoint(), eventInfo.getIpAddress(),
                                         eventInfo.getMacAddress());
    }

    /**
     * Extract the BNG attachment key given some of the attachment fields.
     *
     * @param onuSerialNumber Serial number of the ONU
     * @param cTag            VLAN C-Tag
     * @param sTag            VLAN S-Tag
     * @param oltConnectPoint The OLT-level connect point
     * @param ipAddress       The attachment IP address
     * @param macAddress      The attachment MAC address
     * @return The built attachment ID
     */
    public static String calculateBngAttachmentKey(String onuSerialNumber,
                                                   VlanId cTag, VlanId sTag,
                                                   ConnectPoint oltConnectPoint,
                                                   IpAddress ipAddress,
                                                   MacAddress macAddress) {
        return String.join("/", onuSerialNumber, cTag.toString(),
                           sTag.toString(), oltConnectPoint.toString(),
                           macAddress.toString()
        );
    }
}
