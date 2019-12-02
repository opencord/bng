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

import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.BngProgrammable;

import java.util.List;

/**
 * BNG service for setting up and removing attachments.
 */
public interface BngService {

    String AUX_LOCATIONS_ANNOTATION = "aux-locations";
    String ONU_ANNOTATION = "onu";

    /**
     * Sets up the given attachment in the BNG app. If the attachment is
     * already registered it will be updated. It will also trigger the
     * termination of the attachment user traffic on the ASG device.
     *
     * @param attachment The attachment to be installed or updated
     */
    void setupAttachment(BngProgrammable.Attachment attachment);

    /**
     * Removes an attachment given its attachment ID. It will also trigger the
     * removal of all the related attachment flows from the ASG device.
     *
     * @param attachmentId The ID of the attachment to be removed
     */
    void removeAttachment(BngProgrammable.AttachmentId attachmentId);

    /**
     * Returns all the registered attachments.
     *
     * @return The list of the attachments. Empty list if no attachment is
     * registered.
     */
    List<BngProgrammable.Attachment> getAttachments();

    /**
     * Returns the registered attachment given the ID.
     *
     * @param attachmentId The attachment ID
     * @return The attachment if it is present, null otherwise
     */
    BngProgrammable.Attachment getAttachment(BngProgrammable.AttachmentId attachmentId);

    /**
     * Returns the BNG device ID currently used.
     *
     * @return The BNG device ID.
     */
    DeviceId getBngDeviceId();
}