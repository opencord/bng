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
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.behaviour.BngProgrammable;
import org.opencord.bng.BngService;
import org.opencord.bng.attachment.PppoeAttachment;

@Service
@Command(scope = "bng", name = "attachment-enable",
        description = "Enable/Disable an attachment")
public class EnableAttachment extends AbstractShellCommand {

    @Argument(index = 0, name = "attachmentId", description = "Attachment ID", required = true)
    @Completion(AttachmentIdCompleter.class)
    Long aId = 0L;

    @Option(name = "-d", aliases = "--disable", description = "Disable the specified attachment",
            required = false, multiValued = false)
    boolean disable = false;

    @Override
    protected void doExecute() throws Exception {
        BngService bngService = AbstractShellCommand.get(BngService.class);

        BngProgrammable.AttachmentId attachmentId = new BngProgrammable.AttachmentId(aId);
        BngProgrammable.Attachment attachment = bngService.getAttachment(attachmentId);

        if (attachment == null) {
            print("Attachment " + attachmentId.toString() + "not found!");
            return;
        }
        if (attachment.lineActive() == !disable) {
            print("Attachment is already " + (disable ? "disabled" : "enabled"));
            return;
        }
        if (!attachment.type().equals(BngProgrammable.Attachment.AttachmentType.PPPoE)) {
            print((disable ? "Disable" : "Enable") + " supported only for PPPoE attachment");
            return;
        }

        PppoeAttachment newAttachment = PppoeAttachment.builder()
                .withAttachmentId(attachmentId)
                .withApplicationId(attachment.appId())
                .withMacAddress(attachment.macAddress())
                .withCTag(attachment.cTag())
                .withSTag(attachment.sTag())
                .withPppoeSessionId(attachment.pppoeSessionId())
                .withIpAddress(attachment.ipAddress())
                .withOltConnectPoint(((PppoeAttachment) attachment).oltConnectPoint())
                .withOnuSerial(((PppoeAttachment) attachment).onuSerial())
                .lineActivated(!disable)
                .build();
        print(disable ? "Disabling" : "Enabling" + " attachment: " + newAttachment.toString());
        bngService.setupAttachment(newAttachment);
    }
}