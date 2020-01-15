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

package org.opencord.bng.impl;

public final class OsgiPropertyConstants {

    public static final String ENABLE_LOCAL_EVENT_HANDLER = "enableLocalEventHandler";
    public static final boolean ENABLE_LOCAL_EVENT_HANDLER_DEFAULT = true;
    public static final String BNG_STATISTICS_PROBE_RATE = "bngStatisticsProbeRate";
    public static final long BNG_STATISTICS_PROBE_RATE_DEFAULT = 5000;

    private OsgiPropertyConstants() {
    }

}
