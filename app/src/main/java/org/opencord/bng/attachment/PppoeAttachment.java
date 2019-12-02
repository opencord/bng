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

import com.google.common.base.Objects;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.behaviour.BngProgrammable.Attachment;
import org.onosproject.net.behaviour.BngProgrammable.AttachmentId;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Contains all the information about an attachment in the PPPoE BNG context.
 */
public final class PppoeAttachment implements Attachment {
    // ----------- Standard attachment attributes -----------
    // The ID of the attachment
    private AttachmentId attachmentId;
    // The application that created this attachment
    private ApplicationId appId;
    // The VLAN S-TAG
    private VlanId sTag;
    // The VLAN C-TAG
    private VlanId cTag;
    // The MAC address of the attachment
    private MacAddress macAddress;
    // The IP address of the attachment
    private IpAddress ipAddress;
    // The PPPoE session ID
    private short pppoeSessionId;
    // Define if the attachment is activated
    private boolean lineActivated;

    // -------- PPPoE specific attachments attributes --------
    // The connect point of the OLT
    private ConnectPoint oltConnectPoint;
    // The serial string of the ONU
    private String onuSerial;
    // The QinQ Tpid for the packets
    private short qinqTpid;

    private PppoeAttachment(AttachmentId attachmentId, ApplicationId appId, VlanId sTag, VlanId cTag,
                            MacAddress macAddress, IpAddress ipAddress, boolean lineActivated,
                            short pppoeSessionId, ConnectPoint oltConnectPoint, String onuSerial,
                            short qinqTpid) {
        this.attachmentId = attachmentId;
        this.appId = appId;
        this.sTag = sTag;
        this.cTag = cTag;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.lineActivated = lineActivated;
        this.pppoeSessionId = pppoeSessionId;
        this.oltConnectPoint = oltConnectPoint;
        this.onuSerial = onuSerial;
        this.qinqTpid = qinqTpid;
    }

    public static Builder builder() {
        return new Builder();
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

    @Override
    public AttachmentId attachmentId() {
        return this.attachmentId;
    }

    @Override
    public AttachmentType type() {
        return AttachmentType.PPPoE;
    }

    @Override
    public short pppoeSessionId() {
        return this.pppoeSessionId;
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
        return toStringHelper(this)
                .add("AttachmentId", this.attachmentId)
                .add("ApplicationId", this.appId)
                .add("S-Tag", this.sTag)
                .add("C-Tag", this.cTag)
                .add("MacAddress", this.macAddress)
                .add("IpAddress", this.ipAddress)
                .add("PPPoESessionId", this.pppoeSessionId)
                .add("LineActivated", this.lineActivated)
                .add("ConnectPoint", this.oltConnectPoint)
                .add("OnuSerial", this.onuSerial)
                .add("QinQTpid", this.qinqTpid)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(attachmentId, appId, sTag, cTag, macAddress, ipAddress, pppoeSessionId, lineActivated, oltConnectPoint, onuSerial, qinqTpid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PppoeAttachment other = (PppoeAttachment) obj;
        return Objects.equal(this.attachmentId, other.attachmentId)
                && Objects.equal(this.appId, other.appId)
                && Objects.equal(this.sTag, other.sTag)
                && Objects.equal(this.cTag, other.cTag)
                && Objects.equal(this.macAddress, other.macAddress)
                && Objects.equal(this.ipAddress, other.ipAddress)
                && Objects.equal(this.pppoeSessionId, other.pppoeSessionId)
                && Objects.equal(this.lineActivated, other.lineActivated)
                && Objects.equal(this.oltConnectPoint, other.oltConnectPoint)
                && Objects.equal(this.onuSerial, other.onuSerial)
                && Objects.equal(this.qinqTpid, other.qinqTpid);
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

        public Builder addOnuSerial(String onuSerial) {
            this.onuSerial = onuSerial;
            return this;
        }

        public Builder addOltConnectPoint(ConnectPoint oltconnectPoint) {
            this.oltconnectPoint = oltconnectPoint;
            return this;
        }

        public Builder addSTag(VlanId sTag) {
            this.sTag = sTag;
            return this;
        }

        public Builder addCTag(VlanId cTag) {
            this.cTag = cTag;
            return this;
        }

        public Builder addPppoeSessionId(short pppoeSessionId) {
            this.pppoeSessionId = pppoeSessionId;
            return this;
        }

        public Builder addAttachmentId(long id) {
            this.attachmentId = new AttachmentId(id);
            return this;
        }

        public Builder addAttachmentId(AttachmentId id) {
            this.attachmentId = id;
            return this;
        }

        public Builder lineActivated(boolean lineActivated) {
            this.lineActivated = lineActivated;
            return this;
        }

        public Builder addApplicationId(ApplicationId appId) {
            this.appId = appId;
            return this;
        }

        public Builder addMacAddress(MacAddress macAddress) {
            this.macAddress = macAddress;
            return this;
        }

        public Builder addIpAddress(IpAddress ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder addQinqTpid(short qinqTpid) {
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
