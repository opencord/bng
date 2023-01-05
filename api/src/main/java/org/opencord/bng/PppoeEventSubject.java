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

package org.opencord.bng;

import com.google.common.base.MoreObjects;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.net.ConnectPoint;

/**
 * Subject for a PPPoE attachment level events.
 */
public final class PppoeEventSubject {

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
     * @param oltConnectPoint The connect point of the OLT (UNI port)
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
     * Returns the connect point of the OLT (UNI port).
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
