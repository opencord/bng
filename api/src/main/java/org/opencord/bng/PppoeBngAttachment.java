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

import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.behaviour.BngProgrammable;

/**
 * Contains all the information about an attachment in the PPPoE BNG context.
 */
public final class PppoeBngAttachment extends BngAttachment {

    // The PPPoE session ID
    private final short pppoeSessionId;

    private PppoeBngAttachment(ApplicationId appId, VlanId sTag, VlanId cTag,
                               MacAddress macAddress, IpAddress ipAddress, boolean lineActivated,
                               short pppoeSessionId, ConnectPoint oltConnectPoint, String onuSerial,
                               short qinqTpid) {
        super(appId, sTag, cTag, macAddress, ipAddress, lineActivated,
              oltConnectPoint, onuSerial, qinqTpid);
        this.pppoeSessionId = pppoeSessionId;
    }

    /**
     * Returns a builder for PPPoE BNG Attachemnt.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // TODO: remove when BngProgrammable API are updated!
    @Override
    public BngProgrammable.AttachmentId attachmentId() {
        return null;
    }

    @Override
    public BngProgrammable.Attachment.AttachmentType type() {
        return BngProgrammable.Attachment.AttachmentType.PPPoE;
    }

    @Override
    public short pppoeSessionId() {
        return this.pppoeSessionId;
    }

    @Override
    public String toString() {
        return this.toStringHelper()
                .add("pppoeSessionId", pppoeSessionId())
                .toString();
    }

    /**
     * Builder of PPPoE attachments.
     */
    public static final class Builder extends BngBuilder {
        private short pppoeSessionId = 0;

        private Builder() {
            super();
        }

        /**
         * Sets the PPPoE session ID.
         *
         * @param pppoeSessionId The PPPoE session ID
         * @return self
         */
        public Builder withPppoeSessionId(short pppoeSessionId) {
            this.pppoeSessionId = pppoeSessionId;
            return this;
        }

        /**
         * Returns a new PPPoE attachment.
         *
         * @return PPPoE attachment
         */
        public PppoeBngAttachment build() {
            return new PppoeBngAttachment(this.appId,
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
