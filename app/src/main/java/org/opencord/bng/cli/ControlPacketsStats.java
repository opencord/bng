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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.pi.runtime.PiCounterCellData;
import org.opencord.bng.BngStatisticsService;

@Service
@Command(scope = "bng", name = "control-stats",
        description = "Retrieve statistics of control plane packets of un-registered attachments")
public class ControlPacketsStats extends AbstractShellCommand {

    @Override
    protected void doExecute() throws Exception {
        BngStatisticsService bngStatisticsService = AbstractShellCommand.get(BngStatisticsService.class);
        PiCounterCellData stats = bngStatisticsService.getControlStats();
        if (stats != null) {
            print("Packets: " + stats.packets());
            print("Bytes:\t" + stats.bytes());
        } else {
            print("NO BNG programmable available");
        }
    }
}
