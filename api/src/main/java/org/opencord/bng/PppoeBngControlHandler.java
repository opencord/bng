package org.opencord.bng;

import org.onosproject.event.ListenerService;
import org.opencord.bng.events.PppoeEvent;
import org.opencord.bng.events.PppoeEventListener;

/**
 * BNG control packet service for PPPoE protocol.
 */
public interface PppoeBngControlHandler
        extends ListenerService<PppoeEvent, PppoeEventListener> {
}
