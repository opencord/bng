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

import org.onosproject.net.DeviceId;

import java.util.Map;

/**
 * Service for managing attachments.
 */
public interface BngService {

    String ONU_ANNOTATION = "onu";

    /**
     * Sets up the given attachment with the given attachemtn key in the BNG
     * app. If the attachment is already registered it will be updated. It will
     * also trigger the termination of the attachment user traffic on the ASG
     * device.
     *
     * @param attachmentKey The key for the given attachment
     * @param attachment    The attachment to be installed or updated
     */
    void setupAttachment(String attachmentKey, BngAttachment attachment);

    /**
     * Removes an attachment given its attachment ID. It will also trigger the
     * removal of all the related attachment flows from the ASG device.
     *
     * @param attachmentKey The ID of the attachment to be removed
     */
    void removeAttachment(String attachmentKey);

    /**
     * Returns a map with the registered attachments.
     *
     * @return The map of attachemtn keys and attachments. Empty map if no
     * attachment is registered.
     */
    Map<String, BngAttachment> getAttachments();

    /**
     * Returns the registered attachment given the ID.
     *
     * @param attachmentKey The attachment ID
     * @return The attachment if it is present, null otherwise
     */
    BngAttachment getAttachment(String attachmentKey);

    /**
     * Returns the BNG device ID currently used.
     *
     * @return The BNG device ID.
     */
    DeviceId getBngDeviceId();
}