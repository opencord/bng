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
