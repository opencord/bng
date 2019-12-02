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
import org.opencord.bng.BngService;

@Service
@Command(scope = "bng", name = "attachment-remove",
        description = "Remove an attachment from the BNG")
public class RemoveAttachment extends AbstractShellCommand {

    @Argument(index = 0, name = "attachmentKey", description = "Attachment Key. No Key or 0 means ALL")
    @Completion(AttachmentKeyCompleter.class)
    String attachmentKey = null;

    @Override
    protected void doExecute() throws Exception {
        BngService bngService = AbstractShellCommand.get(BngService.class);
        if (attachmentKey == null) {
            bngService.getAttachments().keySet()
                    .forEach(bngService::removeAttachment);
            return;
        }
        bngService.removeAttachment(attachmentKey);
        print("Attachment removed");
    }
}
