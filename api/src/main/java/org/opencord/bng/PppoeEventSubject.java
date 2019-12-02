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

package org.opencord.bng;

import com.google.common.base.MoreObjects;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.net.ConnectPoint;

/**
 * Subject for a PPPoE attachment level events.
 */
public class PppoeEventSubject {

    // TODO: Move to Kafka APP.
//    private static final String TOPIC = "pppoe.events";
//    private static final String TIMESTAMP = "timestamp";
//    private static final String EVENT_TYPE = "eventType";
//    private static final String OLT_DEVICE_ID = "deviceId";
//    private static final String OLT_PORT_NUMBER = "portNumber";
//    private static final String MAC_ADDRESS = "macAddress";
//    private static final String IP_ADDRESS = "ipAddress";
//    private static final String SERIAL_NUMBER = "serialNumber";
//    private static final String SESSION_ID = "sessionId";

    private final ConnectPoint oltConnectPoint;
    private final IpAddress ipAddress;
    private final MacAddress macAddress;
    private final String onuSerialNumber;
    private final short sessionId;
    private final VlanId sTag;
    private final VlanId cTag;

    /**
     * Creates a PPPoE attachment event subject.
     *
     * @param oltConnectPoint The OLT-level connect point
     * @param ipAddress       The IP Address that has been assigned
     * @param macAddress      The MAC address of the attachment
     * @param onuSerialNumber The serial number of the ONU
     * @param sessionId       The PPPoE session ID
     * @param sTag            The VLAN S-Tag
     * @param cTag            The VLan C-Tag
     */
    public PppoeEventSubject(ConnectPoint oltConnectPoint,
                             IpAddress ipAddress,
                             MacAddress macAddress,
                             String onuSerialNumber,
                             short sessionId,
                             VlanId sTag,
                             VlanId cTag) {
        this.oltConnectPoint = oltConnectPoint;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.onuSerialNumber = onuSerialNumber;
        this.sessionId = sessionId;
        this.sTag = sTag;
        this.cTag = cTag;
    }

    /**
     * Returns the VOLTHA PON device connect point of the current PPPoE
     * attachment.
     *
     * @return The connect point
     */
    public ConnectPoint getOltConnectPoint() {
        return this.oltConnectPoint;
    }


    /**
     * Returns the assigned IP address (if any).
     *
     * @return The IP address
     */
    public IpAddress getIpAddress() {
        return this.ipAddress;
    }

    /**
     * Returns the MAC address.
     *
     * @return The MAC address
     */
    public MacAddress getMacAddress() {
        return this.macAddress;
    }

    /**
     * Returns the ONU serial number.
     *
     * @return The ONU serial number
     */
    public String getOnuSerialNumber() {
        return onuSerialNumber;
    }

    /**
     * Returns the session ID.
     *
     * @return The session ID.
     */
    public short getSessionId() {
        return this.sessionId;
    }

    /**
     * Returns the VLAN S-Tag.
     *
     * @return The VLAN S-Tag.
     */
    public VlanId getsTag() {
        return sTag;
    }

    /**
     * Returns the VLAN C-Tag.
     *
     * @return The VLAN C-Tag.
     */
    public VlanId getcTag() {
        return cTag;
    }

    /**
     * Serialize the event in JSON.
     *
     * @return Json serialization
     */
    // TODO: move to kafka app.
//    public JsonNode serializeBngEvent() {
//        // Serialize PPPoE event in a JSON node
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectNode pppoeEvent = mapper.createObjectNode();
//        pppoeEvent.put(TIMESTAMP, Instant.now().toString());
//        pppoeEvent.put(EVENT_TYPE, this.getType().toString());
//        pppoeEvent.put(OLT_DEVICE_ID, this.getOltConnectPoint().deviceId().toString());
//        pppoeEvent.put(OLT_PORT_NUMBER, this.getOltConnectPoint().port().toString());
//        pppoeEvent.put(MAC_ADDRESS, this.getMacAddress().toString());
//        pppoeEvent.put(IP_ADDRESS, this.getIpAddress().toString());
//        pppoeEvent.put(SERIAL_NUMBER, this.getOnuSerialNumber());
//        pppoeEvent.put(SESSION_ID, this.getSessionId() == 0 ? "" : String.valueOf(this.getSessionId()));
//        return pppoeEvent;
//    }
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("oltConnectPoint", oltConnectPoint)
                .add("ipAddress", ipAddress)
                .add("macAddress", macAddress)
                .add("onuSerialNumber", onuSerialNumber)
                .add("sessionId", sessionId)
                .add("STag", sTag)
                .add("CTag", cTag)
                .toString();
    }
}
