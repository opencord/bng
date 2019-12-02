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
import org.onosproject.net.behaviour.BngProgrammable;

/**
 * Standard attachment implementation in the BNG application.
 */
public abstract class AbstractAttachment implements BngProgrammable.Attachment {
    // ----------- Standard attachment attributes -----------
    // The ID of the attachment
    private final BngProgrammable.AttachmentId attachmentId;
    // The application that created this attachment
    private final ApplicationId appId;
    // The VLAN S-TAG
    private final VlanId sTag;
    // The VLAN C-TAG
    private final VlanId cTag;
    // The MAC address of the attachment
    private final MacAddress macAddress;
    // The IP address of the attachment
    private final IpAddress ipAddress;
    // The PPPoE session ID
    private final short pppoeSessionId;
    // Define if the attachment is activated
    private final boolean lineActivated;

    public AbstractAttachment(BngProgrammable.AttachmentId attachmentId,
                              ApplicationId appId, VlanId sTag, VlanId cTag,
                              MacAddress macAddress, IpAddress ipAddress,
                              short pppoeSessionId, boolean lineActivated) {
        this.attachmentId = attachmentId;
        this.appId = appId;
        this.sTag = sTag;
        this.cTag = cTag;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.pppoeSessionId = pppoeSessionId;
        this.lineActivated = lineActivated;
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
    public BngProgrammable.AttachmentId attachmentId() {
        return this.attachmentId;
    }

    @Override
    public BngProgrammable.Attachment.AttachmentType type() {
        return BngProgrammable.Attachment.AttachmentType.PPPoE;
    }

    @Override
    public short pppoeSessionId() {
        return this.pppoeSessionId;
    }
}
