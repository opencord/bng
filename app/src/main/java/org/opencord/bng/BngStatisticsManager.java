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
import org.onosproject.event.AbstractListenerManager;
import org.onosproject.net.behaviour.BngProgrammable;
import org.onosproject.net.pi.runtime.PiCounterCellData;
import org.opencord.bng.events.BngStatsEvent;
import org.opencord.bng.events.BngStatsEventInfo;
import org.opencord.bng.events.BngStatsEventListener;
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
        service = BngStatisticsService.class,
        property = {
                BNG_STATISTICS_PROBE_RATE + ":Long=" + BNG_STATISTICS_PROBE_RATE_DEFAULT,
        }
)
public class BngStatisticsManager
        extends AbstractListenerManager<BngStatsEvent, BngStatsEventListener> implements BngStatisticsService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BngStatisticsMonitor bngStatsMonitor = new BngStatisticsMonitor();

    /**
     * The BNG statistics probe rate.
     */
    private long bngStatisticsProbeRate = BNG_STATISTICS_PROBE_RATE_DEFAULT;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private ComponentConfigService componentConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private BngService bngManager;

    private ScheduledFuture<?> timeout;

    @Activate
    protected void activate() {
        eventDispatcher.addSink(BngStatsEvent.class, listenerRegistry);
        componentConfigService.registerProperties(getClass());
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
        attachments.forEach(attachment -> stats.put(attachment, getStatistics(attachment)));
        return stats;
    }

    @Override
    public Map<BngProgrammable.BngCounterType, PiCounterCellData> getStatistics(BngProgrammable.Attachment attachment) {
        BngProgrammable bngProgrammable = bngManager.getBngProgrammable();
        if (bngProgrammable != null) {
            try {
                return bngProgrammable.readCounters(attachment);
            } catch (BngProgrammable.BngProgrammableException e) {
                log.error("Error getting statistics of {}", attachment);
            }
        }
        return Maps.newHashMap();
    }

    @Override
    public PiCounterCellData getControlStats() {
        BngProgrammable bngProgrammable = bngManager.getBngProgrammable();
        if (bngProgrammable != null) {
            try {
                return bngProgrammable.readControlTrafficCounter();
            } catch (BngProgrammable.BngProgrammableException e) {
                log.error("Error control plane packets statistics");
            }
        }
        return null;
    }


    private class BngStatisticsMonitor implements Runnable {
        @Override
        public void run() {
            //Generate events on
            BngProgrammable bngProgrammable = bngManager.getBngProgrammable();
            if (bngProgrammable != null) {
                log.debug("READING stats!");
                List<BngProgrammable.Attachment> attachments = bngManager.getAttachments();
                Map<BngProgrammable.Attachment, Map<BngProgrammable.BngCounterType, PiCounterCellData>>
                        attachmentsStats = getStatistics(attachments);
                if (!attachmentsStats.isEmpty()) {
                    BngStatsEventInfo evInfo = new BngStatsEventInfo(attachmentsStats);
                    post(new BngStatsEvent(BngStatsEvent.EventType.STATISTICS, evInfo));
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


