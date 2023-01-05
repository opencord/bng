/*
 * Copyright 2019-2023 Open Networking Foundation (ONF) and the ONF Contributors
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

import org.onosproject.event.AbstractEvent;

/**
 * Represents an event related to attachment-level statistics.
 */
public final class BngStatsEvent extends
        AbstractEvent<BngStatsEvent.EventType, BngStatsEventSubject> {

    /**
     * Creates an attachment-level statistics event.
     *
     * @param type    Event type
     * @param subject Event subject
     */
    public BngStatsEvent(EventType type, BngStatsEventSubject subject) {
        super(type, subject);
    }

    /**
     * Type of the event.
     */
    public enum EventType {
        /**
         * Signals that the statistics has been updated.
         */
        STATS_UPDATED
    }
}
