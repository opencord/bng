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
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.behaviour.BngProgrammable;
import org.opencord.bng.BngService;
import org.opencord.bng.BngStatsService;

import static java.util.Map.Entry.comparingByKey;

@Service
@Command(scope = "bng", name = "attachment-stats",
        description = "Get the stats (registers) of the attachments")
public class AttachmentStats extends AbstractShellCommand {

    @Argument(index = 0, name = "attachmentId", description = "Attachment ID. No ID or 0 means ALL")
    @Completion(AttachmentIdCompleter.class)
    Long attachmentId = Long.valueOf(0);

    @Override
    protected void doExecute() throws Exception {
        BngService bngService = AbstractShellCommand.get(BngService.class);

        print("STATISTICS");
        if (attachmentId == 0) {
            // Print the statistics for all the registered attachments
            bngService.getAttachments().forEach(this::printAttachmentStats);
        } else {
            printAttachmentStats(bngService.getAttachment(new BngProgrammable.AttachmentId(attachmentId)));
        }
    }

    private void printAttachmentStats(BngProgrammable.Attachment attachment) {
        if (attachment != null) {
            BngStatsService bngStatsService = AbstractShellCommand.get(BngStatsService.class);
            print("MAC: " + attachment.macAddress().toString()
                    + "\nC_TAG: " + attachment.cTag().toShort()
                    + "\nS_TAG: " + attachment.sTag().toString()
                    + "\nIP: " + attachment.ipAddress());
            bngStatsService.getStats(attachment.attachmentId()).entrySet().stream().sorted(comparingByKey())
                    .forEach(
                            (entry) -> {
                                print(BngCliUtils.niceCounterName(entry.getKey()));
                                print("\tPackets:" + entry.getValue().packets());
                                print("\tBytes:\t" + entry.getValue().bytes());
                            }
                    );
        }
    }
}