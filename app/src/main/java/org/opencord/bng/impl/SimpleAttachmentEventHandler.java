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

package org.opencord.bng.impl;

import org.glassfish.jersey.internal.guava.Sets;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.store.service.StorageService;
import org.opencord.bng.BngAttachment;
import org.opencord.bng.BngService;
import org.opencord.bng.PppoeBngAttachment;
import org.opencord.bng.PppoeBngControlHandler;
import org.opencord.bng.PppoeEvent;
import org.opencord.bng.PppoeEventListener;
import org.opencord.bng.PppoeEventSubject;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Properties;
import java.util.Set;

import static org.opencord.bng.impl.OsgiPropertyConstants.ENABLE_LOCAL_EVENT_HANDLER;
import static org.opencord.bng.impl.OsgiPropertyConstants.ENABLE_LOCAL_EVENT_HANDLER_DEFAULT;

/**
 * Service to intercept the PPPoE Handler events and trigger the creation of a
 * new attachment in BNG service.
 */
@Component(immediate = true,
        property = {
                ENABLE_LOCAL_EVENT_HANDLER + ":Boolean=" + ENABLE_LOCAL_EVENT_HANDLER_DEFAULT,
        }
)
public class SimpleAttachmentEventHandler {

    private static final String ATTACHMENT_ID_GENERATOR_NAME = "SIMPLE_ATTACHMENT_EVENT_HANDLER_ATTACHMENT_ID";
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService componentConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PppoeBngControlHandler pppoEHandlerRelay;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected BngService bngService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StorageService storageService;

    /**
     * Whether to enable of not the local attachment event handler, for debugging/development.
     */
    private boolean enableLocalEventHandler = ENABLE_LOCAL_EVENT_HANDLER_DEFAULT;
    private InternalPppoeEvent pppoeEventListener = new InternalPppoeEvent();

    // Map to store the attachment that this component has submitted through the BNG Service
    private Set<String> addedAttachmentKeys;

    private ApplicationId appId;

    @Activate
    protected void activate() {
        appId = coreService.getAppId(BngManager.BNG_APP);
        addedAttachmentKeys = Sets.newHashSet();
        componentConfigService.registerProperties(getClass());
        pppoEHandlerRelay.addListener(pppoeEventListener);
        log.info("Simple Attachment Event Handler STARTED");
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();

        Boolean localEvent = Tools.isPropertyEnabled(properties, ENABLE_LOCAL_EVENT_HANDLER);
        if (localEvent != null) {
            enableLocalEventHandler = localEvent;
        }
    }

    @Deactivate
    protected void deactivate() {
        pppoEHandlerRelay.removeListener(pppoeEventListener);
        addedAttachmentKeys = null;
        componentConfigService.unregisterProperties(getClass(), false);
        log.info("Simple Attachment Event Handler STOPPED");
    }

    /**
     * Listener for BNG Attachment event for PPPoE attachments.
     */
    class InternalPppoeEvent implements PppoeEventListener {
        @Override
        public void event(PppoeEvent event) {
            PppoeEventSubject eventInfo = event.subject();
            String attachmentKey = BngUtils.calculateBngAttachmentKey(eventInfo);
            switch (event.type()) {
                case IPCP_CONF_ACK:
                    log.debug("Received IPCP_CONF_ACK event, submit a new attachment");
                    log.debug(eventInfo.toString());
                    BngAttachment newAttachment = PppoeBngAttachment.builder()
                            .withPppoeSessionId(eventInfo.getSessionId())
                            .withApplicationId(appId)
                            .withCTag(eventInfo.getcTag())
                            .withSTag(eventInfo.getsTag())
                            .withIpAddress(eventInfo.getIpAddress())
                            .withMacAddress(eventInfo.getMacAddress())
                            .withOnuSerial(eventInfo.getOnuSerialNumber())
                            .withOltConnectPoint(eventInfo.getOltConnectPoint())
                            .lineActivated(true)
                            .build();
                    if (!addedAttachmentKeys.add(attachmentKey)) {
                        log.warn("Attachment ID already present. Re-submit the attachment");
                    }
                    bngService.setupAttachment(attachmentKey, newAttachment);
                    break;

                case SESSION_TERMINATION:
                    attachmentKey =  BngUtils.calculateBngAttachmentKey(eventInfo);
                    log.debug("Received SESSION_TERMINATION event, remove the attachment {}",
                              attachmentKey);
                    if (!addedAttachmentKeys.remove(attachmentKey)) {
                        log.debug("Received SESSION_TERMINATION event, for attachment {} " +
                                          "but attachment not present in local store", attachmentKey);
                    } else {
                        log.debug("Received SESSION_TERMINATION event, remove the attachment {}",
                                  attachmentKey);
                        bngService.removeAttachment(attachmentKey);
                    }
                    break;
                case AUTH_FAILURE:
                case AUTH_REQUEST:
                case AUTH_SUCCESS:
                case SESSION_INIT:
                case IPCP_CONF_REQUEST:
                case SESSION_CONFIRMATION:
                    log.debug("Received event {}, nothing to do here.", event.type().toString());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + event.type() +
                                                            ", for attachment: " + attachmentKey);
            }
        }

        @Override
        public boolean isRelevant(PppoeEvent event) {
            return enableLocalEventHandler &&
                    event.subject().getClass().equals(PppoeEventSubject.class);
        }
    }
}
