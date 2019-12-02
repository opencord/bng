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

package org.opencord.bng.events;

import org.onosproject.event.AbstractEvent;

/**
 * An attachment level event. This kind of event are used to advertise control level attachment events
 * (e.g., attachment creation, successful authentication etc.)
 * @param <T>
 * @param <E>
 */
public abstract class BngAttachmentEvent<T extends Enum, E extends AttachmentEventInfo> extends AbstractEvent<T, E> {
    protected BngAttachmentEvent(T type, E subject) {
        super(type, subject);
    }
}
