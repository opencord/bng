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

package org.opencord.bng.attachment;

import com.google.common.base.MoreObjects;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.behaviour.BngProgrammable.AttachmentId;
import org.opencord.bng.AbstractAttachment;

/**
 * Contains all the information about an attachment in the PPPoE BNG context.
 */
public final class PppoeAttachment extends AbstractAttachment {

    // -------- PPPoE specific attachments attributes --------
    // The connect point of the OLT (UNI port)
    private final ConnectPoint oltConnectPoint;
    // The serial string of the ONU
    private final String onuSerial;
    // The QinQ Tpid for the packets
    private final short qinqTpid;

    private PppoeAttachment(AttachmentId attachmentId, ApplicationId appId, VlanId sTag, VlanId cTag,
                            MacAddress macAddress, IpAddress ipAddress, boolean lineActivated,
                            short pppoeSessionId, ConnectPoint oltConnectPoint, String onuSerial,
                            short qinqTpid) {
        super(attachmentId, appId, sTag, cTag, macAddress, ipAddress, pppoeSessionId, lineActivated);
        this.oltConnectPoint = oltConnectPoint;
        this.onuSerial = onuSerial;
        this.qinqTpid = qinqTpid;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the OLT connect point.
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
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("attachmentId", attachmentId())
                .add("applicationId", appId())
                .add("sTag", sTag())
                .add("cTag", cTag())
                .add("macAddress", macAddress())
                .add("ipAddress", ipAddress())
                .add("pppoeSessionId", pppoeSessionId())
                .add("lineActivated", lineActive())
                .add("oltConnectPoint", oltConnectPoint)
                .add("onuSerial", onuSerial)
                .add("qinqTpid", qinqTpid)
                .toString();
    }


    public static final class Builder {
        private AttachmentId attachmentId;
        private ApplicationId appId;
        private VlanId sTag;
        private VlanId cTag;
        private MacAddress macAddress;
        private IpAddress ipAddress = IpAddress.valueOf(0);
        private short pppoeSessionId = 0;
        private boolean lineActivated = false;
        private ConnectPoint oltconnectPoint = null;
        private String onuSerial = null;
        private short qinqTpid = 0;

        private Builder() {
            // Hide constructor
            this.lineActivated = false;
        }

        public Builder withOnuSerial(String onuSerial) {
            this.onuSerial = onuSerial;
            return this;
        }

        public Builder withOltConnectPoint(ConnectPoint oltconnectPoint) {
            this.oltconnectPoint = oltconnectPoint;
            return this;
        }

        public Builder withSTag(VlanId sTag) {
            this.sTag = sTag;
            return this;
        }

        public Builder withCTag(VlanId cTag) {
            this.cTag = cTag;
            return this;
        }

        public Builder withPppoeSessionId(short pppoeSessionId) {
            this.pppoeSessionId = pppoeSessionId;
            return this;
        }

        public Builder withAttachmentId(long id) {
            this.attachmentId = new AttachmentId(id);
            return this;
        }

        public Builder withAttachmentId(AttachmentId id) {
            this.attachmentId = id;
            return this;
        }

        public Builder lineActivated(boolean lineActivated) {
            this.lineActivated = lineActivated;
            return this;
        }

        public Builder withApplicationId(ApplicationId appId) {
            this.appId = appId;
            return this;
        }

        public Builder withMacAddress(MacAddress macAddress) {
            this.macAddress = macAddress;
            return this;
        }

        public Builder withIpAddress(IpAddress ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder withQinqTpid(short qinqTpid) {
            this.qinqTpid = qinqTpid;
            return this;
        }

        public PppoeAttachment build() {
            return new PppoeAttachment(this.attachmentId,
                                       this.appId,
                                       this.sTag,
                                       this.cTag,
                                       this.macAddress,
                                       this.ipAddress,
                                       this.lineActivated,
                                       this.pppoeSessionId,
                                       this.oltconnectPoint,
                                       this.onuSerial,
                                       this.qinqTpid);
        }
    }
}
