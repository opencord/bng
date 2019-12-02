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

import org.onosproject.event.ListenerService;
import org.onosproject.net.behaviour.BngProgrammable;
import org.onosproject.net.pi.runtime.PiCounterCellData;
import org.opencord.bng.events.BngStatsEvent;
import org.opencord.bng.events.BngStatsEventListener;

import java.util.Map;

public interface BngStatisticsService
        extends ListenerService<BngStatsEvent, BngStatsEventListener> {

    /**
     * Retrieve the statistics given an attachment. If the attachment is not
     * programmed in the ASG (e.g., when the BNG programmable is not available),
     * it returns an empty map. If one of the counter type is not supported by
     * the ASG device, that specific counter will not be found in the map.
     *
     * @param attachment The attachment
     * @return A map with the type of the counter and the associated statistics
     * coming from the ASG device. Empty map if no BNG programmable is
     * available.
     */
    Map<BngProgrammable.BngCounterType, PiCounterCellData> getStatistics(BngProgrammable.Attachment attachment);

    /**
     * Retrieve the statistics of attachment that are still not correctly
     * registered. These statistics account for packets that are sent from an
     * attachment while the attachment is negotiating the session (e.g., while
     * authenticating).
     *
     * @return The statistics of not registered attachment. null if no BNG
     * programmable device is available.
     */
    PiCounterCellData getControlStats();

}
