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
 * PPPoE attachment level event. This kind of events are used to advertise
 * control level attachment events (e.g., attachment creation, successful
 * authentication etc.)
 */
public final class PppoeEvent extends
        AbstractEvent<PppoeEvent.EventType, PppoeEventSubject> {
    /**
     * Creates a new event for the given attachment information.
     *
     * @param type    type
     * @param subject the attachment event
     */
    public PppoeEvent(EventType type, PppoeEventSubject subject) {
        super(type, subject);
    }

    /**
     * Type of the event.
     */
    public enum EventType {
        /**
         * Signals that a PPPoE session has been initiated from the client.
         */
        SESSION_INIT,

        /**
         * Signal that a PPPoE session has been established.
         */
        SESSION_CONFIRMATION,

        /**
         * Signal that the PPPoE session has been terminated.
         */
        SESSION_TERMINATION,

        /**
         * Signals an IPCP configuration acknowledge event.
         */
        IPCP_CONF_ACK,

        /**
         * Signals an IPCP configuration request event.
         */
        IPCP_CONF_REQUEST,

        /**
         * Authentication initiated.
         */
        AUTH_REQUEST,

        /**
         * Authentication confirmation.
         */
        AUTH_SUCCESS,

        /**
         * Failed authentication.
         */
        AUTH_FAILURE
    }
}