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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.onlab.packet.EthType;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.HostLocation;
import org.onosproject.net.behaviour.BngProgrammable;
import org.onosproject.net.behaviour.BngProgrammable.Attachment;
import org.onosproject.net.behaviour.BngProgrammable.AttachmentId;
import org.onosproject.net.behaviour.BngProgrammable.BngProgrammableException;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.DriverService;
import org.onosproject.net.host.DefaultHostDescription;
import org.onosproject.net.host.HostDescription;
import org.onosproject.net.host.HostProvider;
import org.onosproject.net.host.HostProviderRegistry;
import org.onosproject.net.host.HostProviderService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.provider.ProviderId;
import org.opencord.bng.config.BngConfig;
import org.opencord.bng.attachment.PppoeAttachment;
import org.opencord.olt.AccessDeviceService;
import org.opencord.sadis.SadisService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.onosproject.net.config.basics.SubjectFactories.APP_SUBJECT_FACTORY;

/**
 * This is a BNG application for managing attachments for the PPPoE
 * use case programming the BNG-u via the FabricBngProgrammable.
 */
@Component(immediate = true, service = BngService.class)
public class BngManager implements HostProvider, BngService {
    static final String BNG_APP = "org.opencord.bng";

    private static final ProviderId PROVIDER_ID = new ProviderId("bngapp", BngManager.BNG_APP);

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicBoolean bnguInitialized = new AtomicBoolean(false);
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    SadisService sadisService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    LinkService linkService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    DriverService driverService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    AccessDeviceService accessDeviceService;
    private ConfigFactory<ApplicationId, BngConfig> cfgFactory = new ConfigFactory<>(
            APP_SUBJECT_FACTORY,
            BngConfig.class,
            BngConfig.KEY) {
        @Override
        public BngConfig createConfig() {
            return new BngConfig();
        }
    };
    private BngProgrammable bngProgrammable;
    private DeviceId bngDeviceId;
    private InternalDeviceListener deviceListener;
    private InternalConfigListener cfgListener;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private HostService hostService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private NetworkConfigRegistry cfgService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private HostProviderRegistry providerRegistry;

    private HostProviderService hostProviderService;
    // TODO: add support for other attachment type
    private Map<AttachmentId, Pair<PppoeAttachment, HostId>> registeredAttachment;
    private ApplicationId appId;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(BNG_APP);
        hostProviderService = providerRegistry.register(this);
        registeredAttachment = Maps.newHashMap();
        bngProgrammable = null;
        bngDeviceId = null;
        deviceListener = new InternalDeviceListener();
        cfgListener = new InternalConfigListener();
        cfgService.addListener(cfgListener);
        cfgService.registerConfigFactory(cfgFactory);

        // Update the BNG relay configuration
        updateConfig();

        deviceService.addListener(deviceListener);

        log.info("BNG app activated");
    }

    @Deactivate
    protected void deactivate() {
        providerRegistry.unregister(this);
        if (bngProgrammableAvailable()) {
            try {
                bngProgrammable.cleanUp(appId);
            } catch (BngProgrammableException e) {
                log.error("Error cleaning-up the BNG pipeline, {}", e.getMessage());
            }
        }
        deviceService.removeListener(deviceListener);
        cfgService.removeListener(cfgListener);
        cfgService.unregisterConfigFactory(cfgFactory);
        registeredAttachment = null;
        bnguInitialized.set(false);
        log.info("BNG app deactivated");
    }

    @Override
    public void registerAttachment(Attachment attachment) {
        // FIXME: the update case is not completely clear. Should the programAttachment method clean up the counters?
        assert attachment.type().equals(Attachment.AttachmentType.PPPoE);
        boolean updating = false;
        var alreadyRegAttachment = registeredAttachment.get(attachment.attachmentId());
        if (alreadyRegAttachment == null) {
            log.info("Registering a new attachment: {}", attachment.toString());
        } else if (!attachment.equals(alreadyRegAttachment)) {
            log.info("Updating the attachment: {}", attachment.toString());
            updating = true;
        } else {
            log.info("Attachment already registered: {}", attachment.toString());
            return;
        }
        var pppoeAttachment = (PppoeAttachment) attachment;
        // Retrieve the connect point on the ASG device
        var asgConnectPoint = getAsgConnectPoint(pppoeAttachment.oltConnectPoint()).orElseThrow();
        final HostId hostId = HostId.hostId(attachment.macAddress(), attachment.sTag());
        final HostDescription hostDescription = createHostDescription(attachment.cTag(), attachment.sTag(),
                attachment.macAddress(), attachment.ipAddress(),
                asgConnectPoint, pppoeAttachment.oltConnectPoint(), pppoeAttachment.onuSerial());

        // Make sure that bngProgrammable is available and if so that the attachment is connected to the bngProgrammable
        if (bngProgrammableAvailable() && isCorrectlyConnected(asgConnectPoint)) {
            try {
                programAttachment(attachment, hostId, hostDescription, updating);
            } catch (BngProgrammableException ex) {
                log.error("Attachment not created: " + ex.getMessage());
            }
        } else {
            // If the BNG programmable is not available, or the attachment is not connected to
            // the correct BNG programmable, accept anyway the attachment.
            // Check if the attachment is correctly connected to the BNG device when that device will show up.
            log.info("BNG programmable not available, attachment accepted but not programmed");
        }
        log.info("PPPoE Attachment created/updated: {}", pppoeAttachment);
        registeredAttachment.put(pppoeAttachment.attachmentId(), Pair.of(pppoeAttachment, hostId));
    }

    private Optional<ConnectPoint> getAsgConnectPoint(ConnectPoint oltConnectPoint) {
        try {
            // Here I suppose that each OLT can be connected to a SINGLE ASG that is BNG U capable
            return Optional.of(linkService.getDeviceEgressLinks(oltConnectPoint.deviceId()).stream()
                    .filter(link -> isBngProgrammable(link.dst().deviceId()))
                    .map(link -> link.dst())
                    .collect(Collectors.toList())
                    .get(0));

        } catch (IndexOutOfBoundsException ex) {
            return Optional.empty();
        }
    }

    private Pair<VlanId, VlanId> getVlanTagsFromSadis(String onuSerial) {
        var subscriberInfo = sadisService.getSubscriberInfoService().get(onuSerial);
        if (subscriberInfo == null) {
            return null;
        }
        return Pair.of(subscriberInfo.sTag(), subscriberInfo.cTag());
    }

    /**
     * Setup of an attachment.
     * Before calling this method, make sure that BNG programmable is available.
     *
     * @param attachment
     * @param hostId
     * @param hostDescription
     * @param update
     * @throws BngProgrammableException
     */
    private void programAttachment(Attachment attachment, HostId hostId,
                                   HostDescription hostDescription, boolean update)
            throws BngProgrammableException {
        assert bngProgrammableAvailable();
        bngProgrammable.setupAttachment(attachment);
        if (!update) {
            bngProgrammable.resetCounters(attachment);
        }
        // Trigger host creation in ONOS
        hostProviderService.hostDetected(hostId, hostDescription, true);
    }

    /**
     * Create an host description from the subscriber information.
     *
     * @param cTag            Vlan C-TAG.
     * @param sTag            Vlan S-TAG.
     * @param hostMac         MAC address of the subscriber.
     * @param hostIp          IP address of the subscriber.
     * @param asgConnectPoint Subscriber connect point from the ASG switch
     *                        perspective.
     * @param oltConnectPoint Subscriber connect point from the OLT
     *                        perspective.
     * @param onuSerialNumber ONU Serial Number.
     * @return Host description of the subscriber
     */
    private HostDescription createHostDescription(VlanId cTag, VlanId sTag,
                                                  MacAddress hostMac,
                                                  IpAddress hostIp,
                                                  ConnectPoint asgConnectPoint,
                                                  ConnectPoint oltConnectPoint,
                                                  String onuSerialNumber) {
//        Set<HostLocation> hostLocation = Set.of(new HostLocation(oltConnectPoint.deviceId(),
//                oltConnectPoint.port(),
//                System.currentTimeMillis()));
//        var auxLocation = String.join(",", asgConnectPoint.toString());
        // FIXME: remove this hostLocation and decomment the above rows when aux-location patch will be merged
        Set<HostLocation> hostLocation = Set.of(new HostLocation(asgConnectPoint.deviceId(),
                asgConnectPoint.port(),
                System.currentTimeMillis()));
        var annotations = DefaultAnnotations.builder()
//                .set(AUX_LOCATIONS_ANNOTATION, auxLocation)
                .set(ONU_ANNOTATION, onuSerialNumber)
                .build();
        Set<IpAddress> ips = hostIp != null
                ? ImmutableSet.of(hostIp) : ImmutableSet.of();
        return new DefaultHostDescription(hostMac, sTag,
                hostLocation,
                ips, cTag, EthType.EtherType.QINQ.ethType(),
                false, annotations);
    }

    @Override
    public void removeAttachment(AttachmentId attachmentId) {
        assert attachmentId != null;
        if (!registeredAttachment.containsKey(attachmentId)) {
            log.info("Attachment cannot be removed if it wasn't registered");
            return;
        }
        var regAttachment = registeredAttachment.get(attachmentId).getLeft();

        final HostId hostToBeRemoved = HostId.hostId(regAttachment.macAddress(), regAttachment.sTag());
        registeredAttachment.remove(regAttachment.attachmentId());
        // Try to remove host even if the BNG Programmable is not available
        hostProviderService.hostVanished(hostToBeRemoved);
        if (bngProgrammableAvailable()) {
            bngProgrammable.removeAttachment(regAttachment);
        } else {
            log.info("BNG programmable not available!");
        }
        log.info("Attachment {} removed successfully!", regAttachment);
    }


    @Override
    public List<Attachment> getAttachments() {
        List<Attachment> attachmentList = Lists.newArrayList();
        registeredAttachment.values().forEach(pppoeAttachmentHostIdPair ->
                attachmentList.add(pppoeAttachmentHostIdPair.getLeft()));
        return attachmentList;
    }

    @Override
    public Attachment getAttachment(AttachmentId attachmentId) {
        return registeredAttachment.getOrDefault(attachmentId, Pair.of(null, null))
                .getLeft();
    }

    @Override
    public BngProgrammable getBngProgrammable() {
        return bngProgrammableAvailable() ? this.bngProgrammable : null;
    }

    /**
     * Check if the given connect point is part of the BNG programmable device.
     * Before calling this method, make sure that bngProgrammable is available.
     *
     * @param asgConnectPoint The connect point to check
     * @return
     */
    private boolean isCorrectlyConnected(ConnectPoint asgConnectPoint) {
        assert bngProgrammableAvailable();
        return asgConnectPoint.deviceId().equals(bngProgrammable.data().deviceId());
    }

    /**
     * Setup of the BNG programmable device.
     * This method will cleanup the BNG pipeline, initialize it and then submit all the attachment already registered.
     *
     * @param deviceId BNG programmable device ID
     */
    private void setBngDevice(DeviceId deviceId) {
        synchronized (bnguInitialized) {
            if (bnguInitialized.get()) {
                log.debug("BNG device {} already initialized", deviceId);
                return;
            }
            if (!isBngProgrammable(deviceId)) {
                log.warn("{} is not BNG-U", deviceId);
                return;
            }
            if (bngProgrammable != null && !bngProgrammable.data().deviceId().equals(deviceId)) {
                log.error("Change of the BNG-U while BNG-U device is available is not supported!");
                return;
            }

            bngProgrammable = deviceService.getDevice(deviceId).as(BngProgrammable.class);
            log.info("Program BNG-U device {}", deviceId);

            // Initialize behavior
            try {
                bngProgrammable.init(appId);
                bngProgrammable.cleanUp(appId);
                bngProgrammable.init(appId);
                // FIXME: we can improve this re-registration, keeping track of which subscriber
                //  already has the flow rules submitted in the flow rule subsystem.
                //  In this way we do not need to cleanUp the bngProgrammable every time it come back online.
                // If there is any already registered subscriber, try to re-setup their attachment
                resubmitRegisteredAttachment();

                bnguInitialized.set(true);
            } catch (BngProgrammableException e) {
                log.error("Error in BNG programmable, {}", e.getMessage());
            }
        }
    }

    /**
     * Resubmit all the registered subscriber and their attachment to the BNG programmable device.
     * Before calling this method, make sure that bngProgrammable is available
     *
     * @throws BngProgrammableException
     */
    private void resubmitRegisteredAttachment() throws BngProgrammableException {
        assert bngProgrammableAvailable();
        for (Pair<PppoeAttachment, HostId> regAtt : registeredAttachment.values()) {
            var asgConnectPoint = getAsgConnectPoint(regAtt.getLeft().oltConnectPoint());
            var attachment = (PppoeAttachment) regAtt.getLeft();
            if (asgConnectPoint.isPresent() && isCorrectlyConnected(asgConnectPoint.orElseThrow())) {
                HostDescription hostDescription = createHostDescription(attachment.cTag(), attachment.sTag(),
                        attachment.macAddress(), attachment.ipAddress(),
                        asgConnectPoint.orElseThrow(), attachment.oltConnectPoint(), attachment.onuSerial());
                // When resubmitting registered attachment act as the attachment is being setting up.
                programAttachment(regAtt.getLeft(), regAtt.getRight(), hostDescription, false);
            } else {
                log.info("Attachment is not connected to a valid BNG programmable: {}", attachment);
            }
        }
    }

    /**
     * Unset the BNG programmable device. If available it will be cleaned-up.
     */
    private void unsetBngDevice() {
        synchronized (bnguInitialized) {
            if (bngProgrammable != null) {
                try {
                    bngProgrammable.cleanUp(appId);
                } catch (BngProgrammableException e) {
                    log.error("Error in BNG programmable, {}", e.getMessage());
                }
                bngProgrammable = null;
                bnguInitialized.set(false);
            }
        }
    }

    /**
     * Check if the device is registered and is BNG Programmable.
     *
     * @param deviceId
     * @return
     */
    private boolean isBngProgrammable(DeviceId deviceId) {
        final Device device = deviceService.getDevice(deviceId);
        return device != null && device.is(BngProgrammable.class);
    }

    /**
     * Check if the BNG Programmable is available.
     *
     * @return
     * @throws BngProgrammableException
     */
    private boolean bngProgrammableAvailable() {
        return bngProgrammable != null; // && deviceService.isAvailable(bngProgrammable.data().deviceId());
    }

    private void bngUpdateConfig(BngConfig config) {
        if (config.isValid()) {
            bngDeviceId = config.getBnguDeviceId();
            setBngDevice(bngDeviceId);
        }
    }

    private void bngcRemoveConfig(BngConfig config) {
        unsetBngDevice();
    }

    public DeviceId getBnguDeviceId() {
        return bngDeviceId;
    }

    /**
     * Updates BNG app configuration.
     */
    private void updateConfig() {
        BngConfig bngcRelayConfig =
                cfgService.getConfig(appId, BngConfig.class);
        if (bngcRelayConfig != null) {
            bngUpdateConfig(bngcRelayConfig);
        }
    }

    @Override
    public void triggerProbe(Host host) {
        // Do nothing here
    }

    @Override
    public ProviderId id() {
        return PROVIDER_ID;
    }

    /**
     * React to new devices. The first device recognized to have BNG-U
     * functionality is taken as BNG-U device.
     */
    private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            DeviceId deviceId = event.subject().id();
            if (deviceId.equals(bngDeviceId)) {
                switch (event.type()) {
                    case DEVICE_ADDED:
                    case DEVICE_UPDATED:
                    case DEVICE_AVAILABILITY_CHANGED:
                        // FIXME: do I need the IF?
                        //if (deviceService.isAvailable(deviceId)) {
                        log.warn("Event: {}, SETTING BNG device", event.type());
                        setBngDevice(deviceId);
                        //}
                        break;
                    case DEVICE_REMOVED:
                    case DEVICE_SUSPENDED:
                        unsetBngDevice();
                        break;
                    case PORT_ADDED:
                    case PORT_UPDATED:
                    case PORT_REMOVED:
                    case PORT_STATS_UPDATED:
                        break;
                    default:
                        log.warn("Unknown device event type {}", event.type());
                }
            }
        }
    }


    /**
     * Listener for network config events.
     */
    private class InternalConfigListener implements NetworkConfigListener {
        @Override
        public void event(NetworkConfigEvent event) {
            switch (event.type()) {
                case CONFIG_UPDATED:
                case CONFIG_ADDED:
                    event.config().ifPresent(config -> {
                        bngUpdateConfig((BngConfig) config);
                        log.info("{} updated", config.getClass().getSimpleName());
                    });
                    break;
                case CONFIG_REMOVED:
                    event.prevConfig().ifPresent(config -> {
                        bngcRemoveConfig((BngConfig) config);
                        log.info("{} removed", config.getClass().getSimpleName());
                    });
                    break;
                case CONFIG_REGISTERED:
                case CONFIG_UNREGISTERED:
                    break;
                default:
                    log.warn("Unsupported event type {}", event.type());
                    break;
            }
        }

        @Override
        public boolean isRelevant(NetworkConfigEvent event) {
            if (event.configClass().equals(BngConfig.class)) {
                return true;
            }
            log.debug("Ignore irrelevant event class {}", event.configClass().getName());
            return false;
        }
    }
}
