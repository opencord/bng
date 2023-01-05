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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.onosproject.net.behaviour.BngProgrammable.BngCounterType;
import org.onosproject.net.pi.runtime.PiCounterCellData;

import java.util.Map;

/**
 * Subject for attachment-level statistics events.
 */
public final class BngStatsEventSubject {

    private final BngAttachment bngAttachment;
    private final String attachmentKey;
    private final ImmutableMap<BngCounterType, PiCounterCellData> attachmentStats;

    /**
     * Creates a subject for attachment-level statistics event.
     *
     * @param attachmentKey   The attachment key
     * @param bngAttachment   The attachment instance
     * @param attachmentStats The attachments statistics
     */
    public BngStatsEventSubject(String attachmentKey, BngAttachment bngAttachment,
                                Map<BngCounterType, PiCounterCellData> attachmentStats) {
        this.attachmentKey = attachmentKey;
        this.bngAttachment = bngAttachment;
        this.attachmentStats = ImmutableMap.copyOf(attachmentStats);
    }

    /**
     * Returns an immutable representation of the attachment-level statistics.
     *
     * @return The pair attachment instance and attachment-level statistics
     */
    public Map<BngCounterType, PiCounterCellData> getAttachmentStats() {
        return attachmentStats;
    }

    /**
     * Returns the BNG attachment instance of the attachment-level statistics.
     *
     * @return The BNG attachment instance
     */
    public BngAttachment getBngAttachment() {
        return this.bngAttachment;
    }

    public String getAttachmentKey() {
        return this.attachmentKey;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("attachmentKey", attachmentKey)
                .add("bngAttachment", bngAttachment)
                .add("attachmentsStats", attachmentStats)
                .toString();
    }
}
