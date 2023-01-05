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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.onlab.packet.Data;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onlab.util.ItemNotFoundException;
import org.onlab.util.SharedExecutors;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.event.AbstractListenerManager;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.DriverService;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intf.Interface;
import org.onosproject.net.intf.InterfaceService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.opencord.bng.BngAttachment;
import org.opencord.bng.PppoeBngAttachment;
import org.opencord.bng.PppoeBngControlHandler;
import org.opencord.bng.PppoeEvent;
import org.opencord.bng.PppoeEventListener;
import org.opencord.bng.PppoeEventSubject;
import org.opencord.bng.config.PppoeRelayConfig;
import org.opencord.bng.packets.GenericPpp;
import org.opencord.bng.packets.Ipcp;
import org.opencord.bng.packets.PppProtocolType;
import org.opencord.bng.packets.Pppoe;
import org.opencord.sadis.SadisService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.onosproject.net.config.basics.SubjectFactories.APP_SUBJECT_FACTORY;

@Component(immediate = true)
public class PppoeHandlerRelay
        extends AbstractListenerManager<PppoeEvent, PppoeEventListener>
        implements PppoeBngControlHandler {

    private static final IpAddress IP_ADDRESS_ZERO = IpAddress.valueOf(0);

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final InternalConfigListener cfgListener = new InternalConfigListener();

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected NetworkConfigRegistry cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected InterfaceService interfaceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SadisService sadisService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DriverService driverService;

    private ConfigFactory<ApplicationId, PppoeRelayConfig> cfgFactory = new ConfigFactory<>(
            APP_SUBJECT_FACTORY,
            PppoeRelayConfig.class,
            PppoeRelayConfig.KEY) {
        @Override
        public PppoeRelayConfig createConfig() {
            return new PppoeRelayConfig();
        }
    };
    private ApplicationId appId;
    private InternalPacketProcessor internalPacketProcessor;
    private PppoeRelayConfig pppoeRelayConfig;
    private MacAddress macPppoeServer;

    /**
     * Ephemeral internal map to trace the attachment information. This map is
     * mainly used to modify the packet towards the PPPoE server or towards the
     * attachment.
     * FIXME: this map should be cleaned after some time.
     * FIXME: consider the case of user that moves around
     */
    private Map<MacAddress, BngAttachment> mapSrcMacToAttInfo;

    /**
     * Cache to cache Sadis results during PPPoE connection establishment.
     */
    private final LoadingCache<ImmutableTriple<VlanId, VlanId, ConnectPoint>, ConnectPoint>
            oltCpCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public ConnectPoint load(ImmutableTriple<VlanId, VlanId, ConnectPoint> key) throws Exception {
                    return getOltConnectPoint(key.left, key.middle, key.right).orElseThrow();
                }
            });

    @Activate
    protected void activate() {
        mapSrcMacToAttInfo = Maps.newConcurrentMap();
        appId = coreService.getAppId(BngManager.BNG_APP);
        cfgService.addListener(cfgListener);
        cfgService.registerConfigFactory(cfgFactory);

        eventDispatcher.addSink(PppoeEvent.class, listenerRegistry);

        updateConfig();

        internalPacketProcessor = new InternalPacketProcessor();
        packetService.addProcessor(internalPacketProcessor, PacketProcessor.director(0));

        log.info("PPPoE Handler Relay activated");
    }

    @Deactivate
    protected void deactivate() {
        eventDispatcher.removeSink(PppoeEvent.class);
        packetService.removeProcessor(internalPacketProcessor);
        cfgService.unregisterConfigFactory(cfgFactory);
        oltCpCache.invalidateAll();
        pppoeRelayConfig = null;
        mapSrcMacToAttInfo = null;
        internalPacketProcessor = null;
        macPppoeServer = null;

        log.info("PPPoE Handler Relay deactivated");
    }


    private void updateConfig() {
        PppoeRelayConfig newPppoeRelayConfig = cfgService.getConfig(appId, PppoeRelayConfig.class);
        log.info("{}", newPppoeRelayConfig);
        if (this.pppoeRelayConfig == null &&
                newPppoeRelayConfig != null &&
                newPppoeRelayConfig.isValid()) {
            // TODO: what happens if this is triggered in the middle of a session auth/packet relay?
            this.pppoeRelayConfig = newPppoeRelayConfig;
        }
    }

    private void processPppoePacket(PacketContext context) {
        if (!isConfigured()) {
            log.warn("Missing BNG PPPoE handler relay config. Abort packet processing");
            return;
        }
        Ethernet eth = context.inPacket().parsed();
        log.debug("Parsing the PPPoE header");
        //FIXME: PPPoE and above headers are extracted from the ethernet
        // payload. In case we want to modify the PPPoE/upper-layer header, remember to
        // serialize it back on the Ethernet payload.
        Pppoe pppoe = parsePppoeHeader(eth);
        if (pppoe == null) {
            return;
        }

        log.debug("Processing PPPoE header");

        // Check from where the packet is received and if the interface is configured
        ConnectPoint heardOn = context.inPacket().receivedFrom();
        if (!heardOn.equals(pppoeRelayConfig.getPppoeServerConnectPoint()) &&
                !heardOn.equals(pppoeRelayConfig.getAsgToOltConnectPoint()) &&
                !interfaceService.getInterfacesByPort(heardOn).isEmpty()) {
            log.info("PPPoE packet from unregistered port {}", heardOn);
            return;
        }

        // Retrieve the MAC address of the device that intercepted the packet.
        // This MAC address is the actual PPPoE server MAC address seen by the attachment
        MacAddress bnguMac = interfaceService.getInterfacesByPort(heardOn).iterator().next().mac();

        VlanId cTag = VlanId.vlanId(eth.getVlanID());
        VlanId sTag = VlanId.vlanId(eth.getQinQVID());

        // --------------------------------------- DEBUG ----------------------------------------------
        if (Pppoe.isPPPoED(eth)) {
            log.info("Received {} packet from {}",
                     pppoe.getPacketType(),
                     heardOn);
        }

        StringBuilder logPacketPppoes = new StringBuilder();
        if (Pppoe.isPPPoES(eth)) {
            logPacketPppoes.append("Received PPPoES ")
                    .append(PppProtocolType.lookup(pppoe.getPppProtocol()).type())
                    .append(" packet from ").append(heardOn).append(".");
        }
        if (logPacketPppoes.length() > 0) {
            log.debug(logPacketPppoes.toString());
        }
        log.debug(eth.toString());
        // --------------------------------------------------------------------------------------------

        if (heardOn.equals(pppoeRelayConfig.getPppoeServerConnectPoint())) {
            // DOWNSTREAM PACKET: from the PPPoE server to the attachment.

            // Learn the MAC address of the PPPoE server
            if (macPppoeServer == null) {
                macPppoeServer = eth.getSourceMAC();
            }

            MacAddress dstMac = eth.getDestinationMAC();
            log.debug("Packet to the attachment: {}", eth);
            if (!mapSrcMacToAttInfo.containsKey(dstMac)) {
                BngAttachment newAttInfo = PppoeBngAttachment.builder()
                        .withMacAddress(dstMac)
                        .withSTag(sTag)
                        .withCTag(cTag)
                        .withQinqTpid(eth.getQinQTPID())
                        .build();
                mapSrcMacToAttInfo.put(dstMac, newAttInfo);
            }
            // Retrieve the information about the attachment from the internal MAP
            BngAttachment attInfo = mapSrcMacToAttInfo.get(dstMac);

            // Generate the events for this attachment
            manageAttachmentStateDownstream(eth, pppoe, attInfo);
            modPacketForAttachment(eth, attInfo, bnguMac);

            log.debug("Packet modified as: {}", eth);
            // Send out the packet towards the OLT
            forwardPacket(pppoeRelayConfig.getAsgToOltConnectPoint(), eth);
        } else {
            // UPSTREAM DIRECTION: from the attachment to the PPPoE server
            MacAddress srcMac = eth.getSourceMAC();
            if (!mapSrcMacToAttInfo.containsKey(srcMac)) {
                BngAttachment newAttInfo = PppoeBngAttachment.builder()
                        .withMacAddress(srcMac)
                        .withSTag(sTag)
                        .withCTag(cTag)
                        .withQinqTpid(eth.getQinQTPID())
                        .build();
                mapSrcMacToAttInfo.put(srcMac, newAttInfo);
            }

            manageAttachmentStateUpstream(eth, pppoe);

            modPacketForPPPoEServer(eth);
            log.debug("Packet modified as: {}", eth);
            // Forward packet to the PPPoE server connect point
            forwardPacket(pppoeRelayConfig.getPppoeServerConnectPoint(), eth);
        }
    }

    /**
     * Generate an event related to PPPoE or IPCP state change.
     *
     * @param bngAppEventType Event type
     * @param ip              IP Address if it has been assigned, otherwise
     *                        0.0.0.0
     * @param attInfo         Local attachment information
     */
    private void generateEventPppoe(PppoeEvent.EventType bngAppEventType,
                                    BngAttachment attInfo, short pppoeSessionId,
                                    IpAddress ip) {
        // Retrive the NNI connect point
        ConnectPoint oltConnectPoint;
        try {
            oltConnectPoint = oltCpCache.get(ImmutableTriple.of(attInfo.sTag(), attInfo.cTag(),
                                                                pppoeRelayConfig.getAsgToOltConnectPoint()));
        } catch (ExecutionException e) {
            // If unable to retrieve the OLT Connect Point log error and return.
            // In this way we do NOT propagate the event and eventually create an
            // inconsistent BNG Attachment.
            log.error("Unable to retrieve the OLT Connect Point (\"NNI\" Connect Point)", e);
            return;
        }
        log.info("Generating event of type {}", bngAppEventType);
        post(new PppoeEvent(
                     bngAppEventType,
                     new PppoeEventSubject(
                             oltConnectPoint,
                             ip,
                             attInfo.macAddress(),
                             getPortNameAnnotation(oltConnectPoint),
                             pppoeSessionId,
                             attInfo.sTag(),
                             attInfo.cTag())
             )
        );
    }

    /**
     * Generate attachment related state for the upstream direction.
     *
     * @param eth   The ethernet packet
     * @param pppoe PPPoE header
     */
    private void manageAttachmentStateUpstream(Ethernet eth, Pppoe pppoe) {
        PppoeEvent.EventType eventType = null;
        MacAddress srcMac = eth.getSourceMAC();
        VlanId cTag = VlanId.vlanId(eth.getVlanID());
        VlanId sTag = VlanId.vlanId(eth.getQinQVID());
        BngAttachment attInfo = mapSrcMacToAttInfo.get(srcMac);
        switch (PppProtocolType.lookup(pppoe.getPppProtocol())) {
            case IPCP:
                // Attachment information should be already present
                Ipcp ipcp = (Ipcp) pppoe.getPayload();
                if (ipcp.getCode() == Ipcp.CONF_REQ) {
                    log.debug("IPCP configuration request from attachment");
                    eventType = PppoeEvent.EventType.IPCP_CONF_REQUEST;
                }
                break;
            case NO_PROTOCOL:
                if (Pppoe.isPPPoED(eth) &&
                        pppoe.getPacketType() == Pppoe.PppoeType.PADI) {
                    log.info("PADI received from attachment {}/{}. Saved in internal store",
                             srcMac, sTag);
                    eventType = PppoeEvent.EventType.SESSION_INIT;
                }
                break;
            default:
        }
        if (eventType != null) {
            generateEventPppoe(eventType, attInfo, pppoe.getSessionId(), IP_ADDRESS_ZERO);
        }
    }

    private String getPortNameAnnotation(ConnectPoint oltConnectPoint) {
        return deviceService.getPort(oltConnectPoint.deviceId(),
                                     oltConnectPoint.port()).annotations().value("portName");
    }

    /**
     * Generate attachment related state for the downstream direction.
     *
     * @param eth     The ethernet packet
     * @param pppoe   PPPoE header
     * @param attInfo Attachment info stored in the internal store
     */
    private void manageAttachmentStateDownstream(Ethernet eth, Pppoe pppoe,
                                                 BngAttachment attInfo) {
        PppoeEvent.EventType eventType = null;
        IpAddress assignedIpAddress = IP_ADDRESS_ZERO;
        switch (PppProtocolType.lookup(pppoe.getPppProtocol())) {
            case IPCP:
                Ipcp ipcp = (Ipcp) pppoe.getPayload();
                if (ipcp.getCode() == Ipcp.ACK) {
                    log.info("Received a IPCP ACK from Server. Assigned IP Address {}",
                             ipcp.getIpAddress());
                    assignedIpAddress = ipcp.getIpAddress();
                    eventType = PppoeEvent.EventType.IPCP_CONF_ACK;
                }
                break;

            case CHAP:
                // Check if server has correctly authenticated the attachment
                GenericPpp chap = (GenericPpp) pppoe.getPayload();
                if (chap.getCode() == GenericPpp.CHAP_CODE_SUCCESS) {
                    log.info("CHAP authentication success: {}", attInfo.macAddress());
                    eventType = PppoeEvent.EventType.AUTH_SUCCESS;
                }
                if (chap.getCode() == GenericPpp.CHAP_CODE_FAILURE) {
                    log.info("CHAP authentication failed: {}", attInfo.macAddress());
                    eventType = PppoeEvent.EventType.AUTH_FAILURE;
                }
                break;

            case PAP:
                // Check if server has correctly authenticated the attachment
                GenericPpp pap = (GenericPpp) pppoe.getPayload();
                if (pap.getCode() == GenericPpp.PAP_AUTH_ACK) {
                    log.info("PAP authentication success: {}", attInfo.macAddress());
                    eventType = PppoeEvent.EventType.AUTH_SUCCESS;
                }
                if (pap.getCode() == GenericPpp.PAP_AUTH_NACK) {
                    log.info("PAP authentication failed: {}", attInfo.macAddress());
                    eventType = PppoeEvent.EventType.AUTH_FAILURE;
                }
                break;

            case LCP:
                GenericPpp lcp = (GenericPpp) pppoe.getPayload();
                if (lcp.getCode() == GenericPpp.CODE_TERM_REQ) {
                    log.info("LCP Termination request from PPPoE server");
                    eventType = PppoeEvent.EventType.SESSION_TERMINATION;
                    // When session termination push the correct IP in the event
                    assignedIpAddress = attInfo.ipAddress();
                }
                break;

            case NO_PROTOCOL:
                if (Pppoe.isPPPoED(eth)) {
                    switch (pppoe.getPacketType()) {
                        case PADS:
                            // Set the current PPPoE session ID
                            eventType = PppoeEvent.EventType.SESSION_CONFIRMATION;
                            break;
                        case PADT:
                            log.info("PADT received from PPPoE server");
                            eventType = PppoeEvent.EventType.SESSION_TERMINATION;
                            // When session termination push the correct IP in the event
                            assignedIpAddress = attInfo.ipAddress();
                            break;
                        default:
                    }
                }
                break;
            default:
        }
        // Generate and event if needed
        if (eventType != null) {
            generateEventPppoe(eventType, attInfo, pppoe.getSessionId(), assignedIpAddress);
        }
    }

    private Pppoe parsePppoeHeader(Ethernet eth) {
        try {
            return Pppoe.deserializer().deserialize(((Data) eth.getPayload()).getData(),
                                                    0,
                                                    ((Data) eth.getPayload()).getData().length);
        } catch (DeserializationException e) {
            log.error("Error parsing the PPPoE Headers, packet skipped. \n" + e.getMessage());
            return null;
        }
    }


    /**
     * Apply the modification to the packet to send it to the attachment.
     *
     * @param eth     Packet to be modified
     * @param attInfo Attachment information store in the internal map
     */
    private void modPacketForAttachment(Ethernet eth,
                                        BngAttachment attInfo,
                                        MacAddress newSourceMac) {
        eth.setVlanID(attInfo.cTag().toShort());
        eth.setQinQVID(attInfo.sTag().toShort());
        eth.setQinQTPID(attInfo.qinqTpid());
        eth.setSourceMACAddress(newSourceMac);
    }

    /**
     * Apply the modification to the packet to send it to the PPPoE Server.
     *
     * @param eth Packet to be modified
     */
    private void modPacketForPPPoEServer(Ethernet eth) {
        Set<Interface> interfaces = interfaceService.getInterfacesByPort(pppoeRelayConfig.getPppoeServerConnectPoint());
        if (interfaces != null &&
                interfaces.iterator().hasNext() &&
                interfaces.iterator().next().vlanTagged() != null &&
                interfaces.iterator().next().vlanTagged().iterator().hasNext()) {
            VlanId vlanId = interfaces.iterator().next().vlanTagged().iterator().next();
            if (vlanId != null && vlanId != VlanId.NONE) {
                eth.setVlanID(vlanId.toShort());
                eth.setQinQVID(Ethernet.VLAN_UNTAGGED);
            } else {
                eth.setVlanID(Ethernet.VLAN_UNTAGGED);
                eth.setQinQVID(Ethernet.VLAN_UNTAGGED);
            }
        } else {
            eth.setVlanID(Ethernet.VLAN_UNTAGGED);
            eth.setQinQVID(Ethernet.VLAN_UNTAGGED);
        }
        // Modify DST Mac Address with the one of the PPPoE Server
        if (!eth.getDestinationMAC().isBroadcast()) {
            if (macPppoeServer == null) {
                log.warn("NO Mac address for PPPoE server available! Dropping packet");
                return;
            }
            eth.setDestinationMACAddress(macPppoeServer);
        }
    }

    /**
     * Retrieve the NNI Connect Point given the S-Tag, C-Tag and the OLT facing
     * ASG connect point.
     *
     * @param sTag                 The S-Tag VLAN tag
     * @param cTag                 The C-Tag VLAN tag
     * @param asgToOltConnectPoint Connect point from ASG to OLT.
     * @return
     */
    private Optional<ConnectPoint> getOltConnectPoint(
            VlanId sTag, VlanId cTag, ConnectPoint asgToOltConnectPoint) {
        // Retrieve the UNI port where this attachment is attached to. We assume
        // an attachment is uniquely identified by its c-tag and s-tag in the
        // scope of an OLT. In lack of a better API in SADIS, we retrieve info
        // for all OLT ports and match those that have same c-tag and s-tag as
        // the given attachemnt info.
        var oltDeviceIds = linkService.getIngressLinks(asgToOltConnectPoint)
                .stream()
                .map(link -> link.src().deviceId())
                .filter(deviceId -> {
                    try {
                        return driverService.getDriver(deviceId)
                                .name().contains("voltha");
                    } catch (ItemNotFoundException e) {
                        log.warn("Unable to find driver for {}", deviceId);
                        return false;
                    }
                })
                .collect(Collectors.toSet());

        var oltConnectPoints = oltDeviceIds.stream()
                .flatMap(deviceId -> deviceService.getPorts(deviceId).stream())
                // You could filter the enabled ports only, but this can create
                // problems when ONU are disabled but subscriber is still auth.
                .filter(port -> {
                    var portName = port.annotations().value("portName");
                    // FIXME: here we support a single UNI per ONU port
                    if (portName == null ||
                            (portName.contains("-") && !portName.endsWith("-1"))) {
                        return false;
                    }
                    var subInfo = sadisService.getSubscriberInfoService()
                            .get(portName);
                    return subInfo != null && subInfo.uniTagList().stream()
                            .anyMatch(info -> Objects.equals(cTag, info.getPonCTag()) &&
                                    Objects.equals(sTag, info.getPonSTag()));
                })
                .map(port -> new ConnectPoint(port.element().id(), port.number()))
                .collect(Collectors.toSet());

        if (oltConnectPoints.isEmpty()) {
            log.error("Unable to find a connect point for attachment with S-Tag {} C-Tag {} on OLTs {}",
                      sTag, cTag, oltDeviceIds);
            return Optional.empty();
        } else if (oltConnectPoints.size() > 1) {
            log.error("Multiple OLT connect points found for attachment S-Tag {} C-Tag {}," +
                              "aborting discovery as this is NOT supported (yet)..." +
                              "oltConnectPoints={}",
                      sTag, cTag, oltConnectPoints);
            return Optional.empty();
        }

        return Optional.of(oltConnectPoints.iterator().next());
    }

    /**
     * Send the specified packet, out to the specified connect point.
     *
     * @param toPort Output port to send the packet
     * @param packet Packet to be sent
     */
    private void forwardPacket(ConnectPoint toPort, Ethernet packet) {
        TrafficTreatment toPortTreatment = DefaultTrafficTreatment.builder()
                .setOutput(toPort.port()).build();
        OutboundPacket outboundPacket = new DefaultOutboundPacket(
                toPort.deviceId(), toPortTreatment, ByteBuffer.wrap(packet.serialize()));
        packetService.emit(outboundPacket);
    }

    /**
     * Check if the handler is correctly configured.
     *
     * @return True if it is correctly configure, False otherwise
     */
    private boolean isConfigured() {
        return pppoeRelayConfig != null;
    }

    /**
     * The internal packet processor for PPPoE packets.
     */
    private class InternalPacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext context) {
            processPacketInternal(context);
        }

        private void processPacketInternal(PacketContext context) {
            if (context == null || context.isHandled()) {
                return;
            }
            Ethernet eth = context.inPacket().parsed();
            if (eth == null) {
                return;
            }
            if (!Pppoe.isPPPoES(eth) && !Pppoe.isPPPoED(eth)) {
                return;
            }
            SharedExecutors.getPoolThreadExecutor().submit(() -> {
                try {
                    processPppoePacket(context);
                } catch (Throwable e) {
                    log.error("Exception while processing packet", e);
                }
            });
        }
    }

    /**
     * Listener for network config events.
     */
    private class InternalConfigListener implements NetworkConfigListener {
        @Override
        public void event(NetworkConfigEvent event) {
            switch (event.type()) {
                case CONFIG_ADDED:
                    log.info("CONFIG_ADDED");
                    event.config().ifPresent(config -> {
                        pppoeRelayConfig = ((PppoeRelayConfig) config);
                        log.info("{} added", config.getClass().getSimpleName());
                    });
                    break;
                // TODO: support at least updated and removed events
                case CONFIG_UPDATED:
                case CONFIG_REGISTERED:
                case CONFIG_UNREGISTERED:
                case CONFIG_REMOVED:
                default:
                    log.warn("Unsupported event type {}", event.type());
                    break;
            }
        }

        @Override
        public boolean isRelevant(NetworkConfigEvent event) {
            if (event.configClass().equals(PppoeRelayConfig.class)) {
                return true;
            }
            log.debug("Ignore irrelevant event class {}", event.configClass().getName());
            return false;
        }
    }
}
