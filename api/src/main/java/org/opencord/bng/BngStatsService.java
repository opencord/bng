/*
 * Copyright 2019-2024 Open Networking Foundation (ONF) and the ONF Contributors
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

import org.onosproject.event.ListenerService;
import org.onosproject.net.behaviour.BngProgrammable;
import org.onosproject.net.pi.runtime.PiCounterCellData;

import java.util.Map;

/**
 * Service to retrieve attachment statistics.
 */
public interface BngStatsService
        extends ListenerService<BngStatsEvent, BngStatsEventListener> {

    /**
     * Returns the statistics given an attachment Key. If the attachment is not
     * programmed in the ASG, it returns an empty map. If one of the counter
     * type is not supported by the ASG device, that specific counter will not
     * be found in the map.
     *
     * @param bngAttachmentKey The attachment Key
     * @return A map with the type of the counter and the associated statistics
     * coming from the ASG device. Empty map if no BNG programmable is
     * available.
     */
    Map<BngProgrammable.BngCounterType, PiCounterCellData> getStats(String bngAttachmentKey);

    /**
     * Returns the aggregate statistics related to attachments that are not
     * known by this app, e.g., packets that are sent from an attachment while
     * the attachment is negotiating the session.
     *
     * @return The statistics of not registered attachment. null if no BNG
     * programmable device is available.
     */
    PiCounterCellData getControlStats();

}
