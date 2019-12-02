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
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;

/**
 * Subscriber information for internal store of PPPoE handler relay.
 */
public class PppoeRelayAttachmentInfo {

    private final MacAddress hostMac;
    private final VlanId sTag;
    private final VlanId cTag;
    private final short qinqTpid;

    public PppoeRelayAttachmentInfo(MacAddress hostMac, VlanId sTag, VlanId cTag, short qinqTpid) {
        this.hostMac = hostMac;
        this.sTag = sTag;
        this.cTag = cTag;
        this.qinqTpid = qinqTpid;
    }

    /**
     * Retrieve the attachment MAC address.
     *
     * @return The attachment MAC address
     */
    public MacAddress getAttachmentMac() {
        return hostMac;
    }

    /**
     * Retrieve the attachment S-Tag VLAN tag.
     *
     * @return The S-Tag VLAN tag.
     */
    public VlanId getsTag() {
        return sTag;
    }

    /**
     * Retrieve the attachemnt C-TAg VLAN tag.
     *
     * @return The C-Tag VLAN tag.
     */
    public VlanId getcTag() {
        return cTag;
    }

    /**
     * The QinQ attachment Tpid.
     *
     * @return The QinQ Tpid.
     */
    public short getQinqTpid() {
        return qinqTpid;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mac", hostMac)
                .add("sTag", sTag)
                .add("cTag", cTag)
                .toString();
    }
}
