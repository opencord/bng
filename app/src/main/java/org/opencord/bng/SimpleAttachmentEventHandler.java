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

import com.google.common.collect.Maps;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.core.IdGenerator;
import org.onosproject.net.behaviour.BngProgrammable;
import org.opencord.bng.attachment.PppoeAttachment;
import org.opencord.bng.events.PppoeEventSubject;
import org.opencord.bng.events.PppoeEvent;
import org.opencord.bng.events.PppoeEventListener;
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
import java.util.Map;
import java.util.Properties;

import static org.opencord.bng.OsgiPropertyConstants.ENABLE_LOCAL_EVENT_HANDLER;
import static org.opencord.bng.OsgiPropertyConstants.ENABLE_LOCAL_EVENT_HANDLER_DEFAULT;

/**
 * Service to intercept the PPPoE Handler events and trigger the creation of a
 * new attachment in BNG service.
 */
@Component(immediate = true,
        service = SimpleAttachmentEventHandler.class,
        property = {
                ENABLE_LOCAL_EVENT_HANDLER + ":Boolean=" + ENABLE_LOCAL_EVENT_HANDLER_DEFAULT,
        }
)
public class SimpleAttachmentEventHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Whether to enable of not the local attachment event handler, for
     * debugging/development.
     */
    private boolean enableLocalEventHandler = ENABLE_LOCAL_EVENT_HANDLER_DEFAULT;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService componentConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PppoeBngControlHandler pppoEHandlerRelay;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected BngService bngService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    private InternalPppoeEvent pppoeEventListener = new InternalPppoeEvent();

    // Map to store the attachment that this component has submitted through the BNG Service
    private Map<String, BngProgrammable.AttachmentId> addedAttachment;
    private IdGenerator attachmentIdGenerator;
    private ApplicationId appId;

    @Activate
    protected void activate() {
        appId = coreService.getAppId(BngManager.BNG_APP);
        addedAttachment = Maps.newHashMap();
        componentConfigService.registerProperties(getClass());
        attachmentIdGenerator = coreService.getIdGenerator("SIMPLE_ATTACHMENT_EVENT_HANDLER_ATTACHMENT_ID");
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
        addedAttachment = null;
        attachmentIdGenerator = null;
        componentConfigService.unregisterProperties(getClass(), false);
        log.info("Simple Attachment Event Handler STOPPED");
    }

    private long newAttachmentId() {
        long id = attachmentIdGenerator.getNewId();
        if (id == 0) {
            id = attachmentIdGenerator.getNewId();
        }
        return id;
    }


    /**
     * Listener for BNG Attachment event for PPPoE attachments.
     */
    class InternalPppoeEvent implements PppoeEventListener {
        @Override
        public void event(PppoeEvent event) {
            PppoeEventSubject eventInfo = event.subject();
            BngProgrammable.AttachmentId attachmentId;
            switch (event.type()) {
                case IPCP_CONF_ACK:
                    log.debug("Received IPCP_CONF_ACK event, submit a new attachment");
                    log.debug(eventInfo.toString());
                    attachmentId = new BngProgrammable.AttachmentId(newAttachmentId() % 1000);
                    PppoeAttachment newAttachment = PppoeAttachment.builder()
                            .addApplicationId(appId)
                            .addAttachmentId(attachmentId)
                            .addPppoeSessionId(eventInfo.getSessionId())
                            .addCTag(eventInfo.getcTag())
                            .addSTag(eventInfo.getsTag())
                            .addIpAddress(eventInfo.getIpAddress())
                            .addMacAddress(eventInfo.getMacAddress())
                            .addOnuSerial(eventInfo.getOnuSerialNumber())
                            .addOltConnectPoint(eventInfo.getOltConnectPoint())
                            .lineActivated(true)
                            .build();
                    addedAttachment.put(eventInfo.getOnuSerialNumber(), newAttachment.attachmentId());
                    bngService.setupAttachment(newAttachment);
                    break;

                case SESSION_TERMINATION:
                    attachmentId = addedAttachment.remove(eventInfo.getOnuSerialNumber());
                    log.debug("Received SESSION_TERMINATION event, remove the attachment {}", attachmentId);
                    if (attachmentId != null) {
                        bngService.removeAttachment(attachmentId);
                    } else {
                        log.error("No attachment information in the local store");
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
                    throw new IllegalStateException("Unexpected value: " + event.type());
            }
        }

        @Override
        public boolean isRelevant(PppoeEvent event) {
            return enableLocalEventHandler &&
                    event.subject().getClass().equals(PppoeEventSubject.class);
        }
    }
}
