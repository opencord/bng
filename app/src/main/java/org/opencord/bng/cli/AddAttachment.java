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

package org.opencord.bng.cli;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.cli.net.DeviceIdCompleter;
import org.onosproject.cli.net.PortNumberCompleter;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.opencord.bng.BngAttachment;
import org.opencord.bng.impl.BngManager;
import org.opencord.bng.BngService;
import org.opencord.bng.impl.BngUtils;
import org.opencord.bng.PppoeBngAttachment;

@Service
@Command(scope = "bng", name = "attachment-add",
        description = "Add an attachment on the BNG in disabled state")
public class AddAttachment extends AbstractShellCommand {

    @Argument(index = 0, name = "macAddress", description = "Mac Address of the attachment", required = true)
    String macAddressString = null;

    @Argument(index = 1, name = "ipAddress", description = "IP Address of the attachment", required = true)
    String ipAddressString = null;

    @Argument(index = 2, name = "cTag", description = "VLAN C-TAG of the attachment", required = true)
    short cTag = 0;

    @Argument(index = 3, name = "sTag", description = "VLAN S-TAG of the attachment", required = true)
    short sTag = 0;

    @Argument(index = 4, name = "pppoeSessionId",
            description = "PPPoE session ID of the attachment", required = true)
    short pppoeSessionId = 0;

    @Argument(index = 5, name = "oltDeviceID",
            description = "OLT device ID the attachment is connected to", required = true)
    @Completion(DeviceIdCompleter.class)
    String oltDeviceId = null;

    @Argument(index = 6, name = "portNumber",
            description = "Port number on the OLT device", required = true)
    @Completion(PortNumberCompleter.class)
    String oltPortNumber = null;

    @Argument(index = 7, name = "onuSerial",
            description = "Serial number for the ONU of the attachment", required = true)
    @Completion(OnuSerialCompleter.class)
    String onuSerial = null;

    @Option(name = "-d", aliases = "--disable", description = "Disable the specified attachment",
            required = false, multiValued = false)
    boolean disable = false;


    @Override
    protected void doExecute() throws Exception {
        CoreService coreService = get(CoreService.class);

        ApplicationId appId = coreService.getAppId(BngManager.BNG_APP);
        ConnectPoint uniCp = ConnectPoint.fromString(oltDeviceId + "/" + oltPortNumber);
        MacAddress macAddress = MacAddress.valueOf(macAddressString);
        IpAddress ipAddress = IpAddress.valueOf(ipAddressString);

        String attachmentKey = "CLI" + "/" +
                BngUtils.calculateBngAttachmentKey(onuSerial, VlanId.vlanId(cTag),
                                                   VlanId.vlanId(sTag), uniCp, ipAddress,
                                                   macAddress);

        BngAttachment newAttachment = PppoeBngAttachment.builder()
                .withPppoeSessionId(pppoeSessionId)
                .withApplicationId(appId)
                .withMacAddress(macAddress)
                .withCTag(VlanId.vlanId(cTag))
                .withSTag(VlanId.vlanId(sTag))
                .withIpAddress(ipAddress)
                .withOltConnectPoint(uniCp)
                .withOnuSerial(onuSerial)
                .lineActivated(!disable)
                .build();
        BngService bngService = get(BngService.class);
        bngService.setupAttachment(attachmentKey, newAttachment);
    }
}
