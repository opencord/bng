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

package org.opencord.bng.cli;

import org.onosproject.net.behaviour.BngProgrammable.BngCounterType;

/**
 * Utilities to print counter names in CLI output.
 */
public final class BngCliUtils {

    private BngCliUtils() {
    }

    /**
     * Prints a nicer counter name on CLI output.
     *
     * @param counterName The counter type to be converted
     * @return The string representing the counter
     */
    static String niceCounterName(BngCounterType counterName) {
        switch (counterName) {
            case CONTROL_PLANE:
                return "Upstream Control";
            case DOWNSTREAM_RX:
                return "Downstream Received";
            case DOWNSTREAM_TX:
                return "Downstream Transmitted";
            case UPSTREAM_DROPPED:
                return "Upstream Dropped";
            case UPSTREAM_TX:
                return "Upstream Terminated";
            default:
                return "UNKNOWN";
        }
    }
}
