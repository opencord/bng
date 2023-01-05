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
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.behaviour.BngProgrammable;

/**
 * Abstract implementation of an attachment.
 */
public abstract class BngAttachment implements BngProgrammable.Attachment {
    private final ApplicationId appId;
    private final VlanId sTag;
    private final VlanId cTag;
    private final MacAddress macAddress;
    private final IpAddress ipAddress;
    private final boolean lineActivated;
    private final ConnectPoint oltConnectPoint;
    private final String onuSerial;
    private final short qinqTpid;

    /**
     * Creates a new attachment.
     *
     * @param appId           The application that created this attachment
     * @param sTag            The VLAN S-TAG
     * @param cTag            The VLAN C-TAG
     * @param macAddress      The MAC address of the attachment
     * @param ipAddress       The IP address of the attachment
     * @param lineActivated   Define if the attachment is active or not
     * @param oltConnectPoint The connect point of the OLT (UNI port)
     * @param onuSerial       The serial string of the ONU
     * @param qinqTpid        The QinQ Tpid for the packets
     */
    BngAttachment(ApplicationId appId, VlanId sTag, VlanId cTag,
                  MacAddress macAddress, IpAddress ipAddress,
                  boolean lineActivated, ConnectPoint oltConnectPoint,
                  String onuSerial, short qinqTpid) {
        this.appId = appId;
        this.sTag = sTag;
        this.cTag = cTag;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.lineActivated = lineActivated;
        this.oltConnectPoint = oltConnectPoint;
        this.onuSerial = onuSerial;
        this.qinqTpid = qinqTpid;
    }

    /**
     * Returns the OLT connect point (UNI port).
     *
     * @return The OLT connect point
     */
    public ConnectPoint oltConnectPoint() {
        return this.oltConnectPoint;
    }

    /**
     * Returns the ONU serial number.
     *
     * @return The ONU serial number
     */
    public String onuSerial() {
        return this.onuSerial;
    }

    /**
     * Returns the QinQ TPID.
     *
     * @return The QinQ TPID
     */
    public short qinqTpid() {
        return this.qinqTpid;
    }

    @Override
    public ApplicationId appId() {
        return appId;
    }

    @Override
    public VlanId sTag() {
        return sTag;
    }

    @Override
    public VlanId cTag() {
        return cTag;
    }

    @Override
    public MacAddress macAddress() {
        return macAddress;
    }

    @Override
    public IpAddress ipAddress() {
        return ipAddress;
    }

    @Override
    public boolean lineActive() {
        return lineActivated;
    }

    MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("appId", appId)
                .add("sTag", sTag)
                .add("cTag", cTag)
                .add("macAddress", macAddress)
                .add("ipAddress", ipAddress)
                .add("lineActivated", lineActivated)
                .add("oltConnectPoint", oltConnectPoint)
                .add("onuSerial", onuSerial)
                .add("qinqTpid", qinqTpid);
    }

    /**
     * Abstract builder of attachments.
     */
    public abstract static class BngBuilder {
        ApplicationId appId;
        VlanId sTag;
        VlanId cTag;
        MacAddress macAddress;
        IpAddress ipAddress = IpAddress.valueOf(0);
        boolean lineActivated;
        ConnectPoint oltconnectPoint;
        String onuSerial;
        short qinqTpid;

        BngBuilder() {
            // Hide constructor
            this.lineActivated = false;
        }

        /**
         * Sets the ONU serial number.
         *
         * @param onuSerial The ONU serial number
         * @return self
         */
        public BngBuilder withOnuSerial(String onuSerial) {
            this.onuSerial = onuSerial;
            return this;
        }

        /**
         * Sets the OLT connect point (UNI port).
         *
         * @param oltconnectPoint The OLT connect point
         * @return self
         */
        public BngBuilder withOltConnectPoint(ConnectPoint oltconnectPoint) {
            this.oltconnectPoint = oltconnectPoint;
            return this;
        }

        /**
         * Sets the VLAN S-Tag.
         *
         * @param sTag The VLAN S-Tag
         * @return self
         */
        public BngBuilder withSTag(VlanId sTag) {
            this.sTag = sTag;
            return this;
        }

        /**
         * Sets the VLAN C-Tag.
         *
         * @param cTag the VLAN C-Tag
         * @return self
         */
        public BngBuilder withCTag(VlanId cTag) {
            this.cTag = cTag;
            return this;
        }

        /**
         * Sets the attachment activation state.
         *
         * @param lineActivated The attachment activation state
         * @return self
         */
        public BngBuilder lineActivated(boolean lineActivated) {
            this.lineActivated = lineActivated;
            return this;
        }

        /**
         * Sets the application ID.
         *
         * @param appId The application ID.
         * @return self
         */
        public BngBuilder withApplicationId(ApplicationId appId) {
            this.appId = appId;
            return this;
        }

        /**
         * Sets the attachment MAC address.
         *
         * @param macAddress The MAC address
         * @return self
         */
        public BngBuilder withMacAddress(MacAddress macAddress) {
            this.macAddress = macAddress;
            return this;
        }

        /**
         * Sets the attachment IP address.
         *
         * @param ipAddress The IP address
         * @return self
         */
        public BngBuilder withIpAddress(IpAddress ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        /**
         * Sets the QinQ tpid.
         *
         * @param qinqTpid The QinQ tpid
         * @return self
         */
        public BngBuilder withQinqTpid(short qinqTpid) {
            this.qinqTpid = qinqTpid;
            return this;
        }

        /**
         * Returns a new BNG attachment.
         *
         * @return BNG attachment
         */
        public abstract BngAttachment build();
    }
}
