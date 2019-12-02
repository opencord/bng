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

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import org.onosproject.net.behaviour.BngProgrammable.Attachment;
import org.onosproject.net.behaviour.BngProgrammable.BngCounterType;
import org.onosproject.net.pi.runtime.PiCounterCellData;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Subject for attachment-level statistics events.
 */
public class BngStatsEventSubject {

    private final Map<Attachment, Map<BngCounterType, PiCounterCellData>> attachmentsStats;


    /**
     * Creates a subject for attachment-level statistics event.
     *
     * @param attachmentsStats The attachments statistics.
     */
    public BngStatsEventSubject(Map<Attachment, Map<BngCounterType, PiCounterCellData>> attachmentsStats) {
        this.attachmentsStats = attachmentsStats;
    }

    /**
     * Returns the attachment-level statistics.
     *
     * @return The attachment-level statistics
     */
    public Map<Attachment, Map<BngCounterType, PiCounterCellData>> getStats() {
        return this.attachmentsStats.entrySet().stream()
                .map(e -> Maps.immutableEntry(e.getKey(), Collections.unmodifiableMap(e.getValue())))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("attachmentsStats", attachmentsStats)
                .toString();
    }
}
