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
import org.onlab.util.SharedScheduledExecutors;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.event.AbstractListenerManager;
import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.BngProgrammable;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.pi.runtime.PiCounterCellData;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.opencord.bng.OsgiPropertyConstants.BNG_STATISTICS_PROBE_RATE;
import static org.opencord.bng.OsgiPropertyConstants.BNG_STATISTICS_PROBE_RATE_DEFAULT;

@Component(immediate = true,
        service = BngStatsService.class,
        property = {
                BNG_STATISTICS_PROBE_RATE + ":Long=" + BNG_STATISTICS_PROBE_RATE_DEFAULT,
        }
)
public class BngStatsManager
        extends AbstractListenerManager<BngStatsEvent, BngStatsEventListener> implements BngStatsService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BngStatisticsMonitor bngStatsMonitor = new BngStatisticsMonitor();

    private ApplicationId appId;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService componentConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected BngService bngService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    /**
     * The BNG statistics probe rate.
     */
    private long bngStatisticsProbeRate = BNG_STATISTICS_PROBE_RATE_DEFAULT;
    private ScheduledFuture<?> timeout;

    @Activate
    protected void activate() {
        eventDispatcher.addSink(BngStatsEvent.class, listenerRegistry);
        componentConfigService.registerProperties(getClass());
        appId = coreService.getAppId(BngManager.BNG_APP);
        start();
        log.info("BNG Statistics manager activated");
    }

    @Modified
    protected void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();
        Long probeRate = Tools.getLongProperty(properties, BNG_STATISTICS_PROBE_RATE);
        if (probeRate != null) {
            bngStatisticsProbeRate = probeRate;
        }
    }

    @Deactivate
    protected void deactivate() {
        shutdown();
        componentConfigService.unregisterProperties(getClass(), false);
        eventDispatcher.removeSink(BngStatsEvent.class);
        log.info("BNG Statistics manager deactivated");

    }

    /**
     * Starts the BNG statistics monitor. Does nothing if the monitor is already
     * running.
     */
    private void start() {
        synchronized (bngStatsMonitor) {
            if (timeout == null) {
                timeout = SharedScheduledExecutors.newTimeout(bngStatsMonitor, 0, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Stops the BNG statistics monitor.
     */
    private void shutdown() {
        synchronized (bngStatsMonitor) {
            if (timeout != null) {
                timeout.cancel(true);
                timeout = null;
            }
        }
    }

    private Map<BngProgrammable.Attachment, Map<BngProgrammable.BngCounterType, PiCounterCellData>> getStatistics(
            List<BngProgrammable.Attachment> attachments) {
        Map<BngProgrammable.Attachment, Map<BngProgrammable.BngCounterType, PiCounterCellData>>
                stats = Maps.newHashMap();
        attachments.forEach(attachment -> stats.put(attachment, getStatistics(attachment.attachmentId())));
        return stats;
    }

    @Override
    public Map<BngProgrammable.BngCounterType, PiCounterCellData> getStatistics(
            BngProgrammable.AttachmentId attachmentId) {
        BngProgrammable bngProgrammable = getBngProgrammable(bngService.getBngDeviceId());
        BngProgrammable.Attachment attachment = bngService.getAttachment(attachmentId);
        if (bngProgrammable != null && attachment != null) {
            try {
                return bngProgrammable.readCounters(attachment);
            } catch (BngProgrammable.BngProgrammableException e) {
                log.error("Error getting statistics of {}", attachmentId);
            }
        }
        return Maps.newHashMap();
    }

    @Override
    public PiCounterCellData getControlStats() {
        BngProgrammable bngProgrammable = getBngProgrammable(bngService.getBngDeviceId());
        if (bngProgrammable != null) {
            try {
                return bngProgrammable.readControlTrafficCounter();
            } catch (BngProgrammable.BngProgrammableException e) {
                log.error("Error control plane packets statistics");
            }
        }
        return null;
    }

    private BngProgrammable getBngProgrammable(DeviceId deviceId) {
        if (deviceId != null && deviceService.isAvailable(deviceId)) {
            BngProgrammable bngProgrammable = deviceService.getDevice(deviceId).as(BngProgrammable.class);
            // FIXME: I shouldn't call the init method here, this method is
            //  triggering the punt-to-cpu rules to be installed.
            //  Anyway if not init method is called, the setupBehaviour of
            //  FabricBngProgrammable is never called
            bngProgrammable.init(appId);
            return bngProgrammable;
        }
        return null;
    }

    private class BngStatisticsMonitor implements Runnable {
        @Override
        public void run() {
            BngProgrammable bngProgrammable = getBngProgrammable(bngService.getBngDeviceId());
            if (bngProgrammable != null) {
                List<BngProgrammable.Attachment> attachments = bngService.getAttachments();
                Map<BngProgrammable.Attachment, Map<BngProgrammable.BngCounterType, PiCounterCellData>>
                        attachmentsStats = getStatistics(attachments);
                if (!attachmentsStats.isEmpty()) {
                    BngStatsEventSubject evInfo = new BngStatsEventSubject(attachmentsStats);
                    post(new BngStatsEvent(BngStatsEvent.EventType.UPDATE_STATISTICS, evInfo));
                }
            } else {
                log.debug("BngProgrammable not available");
            }
            synchronized (this) {
                if (timeout != null) {
                    timeout = SharedScheduledExecutors.newTimeout(this, bngStatisticsProbeRate, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}


