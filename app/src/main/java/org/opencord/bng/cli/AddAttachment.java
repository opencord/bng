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
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.behaviour.BngProgrammable;
import org.onosproject.net.device.DeviceService;
import org.opencord.bng.BngManager;
import org.opencord.bng.BngService;
import org.opencord.bng.attachment.PppoeAttachment;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Service
@Command(scope = "bng", name = "attachment-add",
        description = "Add an attachment on the BNG in disabled state")
public class AddAttachment extends AbstractShellCommand {

    @Argument(index = 0, name = "attachmentId", description = "Attachment ID", required = true)
    Long attId = 0L;

    @Argument(index = 1, name = "macAddress", description = "Mac Address of the attachment", required = true)
    String macAddressString = null;

    @Argument(index = 2, name = "ipAddress", description = "IP Address of the attachment", required = true)
    String ipAddressString = null;

    @Argument(index = 3, name = "cTag", description = "VLAN C-TAG of the attachment", required = true)
    short cTag = 0;

    @Argument(index = 4, name = "sTag", description = "VLAN S-TAG of the attachment", required = true)
    short sTag = 0;

    @Argument(index = 5, name = "pppoeSessionId",
            description = "PPPoE session ID of the attachment", required = true)
    short pppoeSessionId = 0;

    @Argument(index = 6, name = "oltDeviceID",
            description = "OLT device ID the attachment is connected to", required = true)
    @Completion(DeviceIdCompleter.class)
    String oltDeviceId = null;

    @Argument(index = 7, name = "portNumber",
            description = "Port number on the OLT device", required = true)
    @Completion(PortNumberCompleter.class)
    String oltPortNumber = null;

    @Argument(index = 8, name = "onuSerial",
            description = "Serial number for the ONU of the attachment", required = true)
    @Completion(OnuSerialCompleter.class)
    String onuSerial = null;

    @Option(name = "-d", aliases = "--disable", description = "Disable the specified attachment",
            required = false, multiValued = false)
    boolean disable = false;


    @Override
    protected void doExecute() throws Exception {
        CoreService coreService = get(CoreService.class);
        BngProgrammable.AttachmentId attachmentId = new BngProgrammable.AttachmentId(attId);
        ApplicationId appId = coreService.getAppId(BngManager.BNG_APP);
        ConnectPoint uniCp = ConnectPoint.fromString(oltDeviceId + "/" + oltPortNumber);

        MacAddress macAddress = MacAddress.valueOf(macAddressString);
        IpAddress ipAddress = IpAddress.valueOf(ipAddressString);
        PppoeAttachment newAttachment = PppoeAttachment.builder()
                .withAttachmentId(attachmentId)
                .withApplicationId(appId)
                .withMacAddress(macAddress)
                .withCTag(VlanId.vlanId(cTag))
                .withSTag(VlanId.vlanId(sTag))
                .withPppoeSessionId(pppoeSessionId)
                .withIpAddress(ipAddress)
                .withOltConnectPoint(uniCp)
                .withOnuSerial(onuSerial)
                .lineActivated(!disable)
                .build();
        BngService bngService = get(BngService.class);
        bngService.setupAttachment(newAttachment);
    }

    private ConnectPoint createConnectPoint(String devicePortString) {
        String[] splitted = devicePortString.split("/");

        checkArgument(splitted.length == 2,
                      "Connect point must be in \"deviceUri/portNumber\" format");

        DeviceId deviceId = DeviceId.deviceId(splitted[0]);
        DeviceService deviceService = get(DeviceService.class);

        List<Port> ports = deviceService.getPorts(deviceId);

        for (Port port : ports) {
            if (splitted[1].equals(port.number().name())) {
                return new ConnectPoint(deviceId, port.number());
            }
        }

        return null;
    }
}
