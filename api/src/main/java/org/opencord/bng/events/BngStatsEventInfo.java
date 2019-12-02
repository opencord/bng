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

package org.opencord.bng.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Host;
import org.onosproject.net.HostLocation;
import org.onosproject.net.behaviour.BngProgrammable.Attachment;
import org.onosproject.net.behaviour.BngProgrammable.BngCounterType;
import org.onosproject.net.host.HostService;
import org.onosproject.net.pi.runtime.PiCounterCellData;
import org.opencord.bng.BngService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BngStatsEventInfo {

    private static final String SUBSCRIBERS = "subscribers";
    private static final String SUBSCRIBER_S_TAG = "sTag";
    private static final String SUBSCRIBER_C_TAG = "cTag";
    private static final String UP_TERM_BYTES = "upTermBytes";
    private static final String UP_TERM_PACKETS = "upTermPackets";
    private static final String UP_DROP_BYTES = "upDropBytes";
    private static final String UP_DROP_PACKETS = "upDropPackets";
    private static final String DOWN_RX_BYTES = "downRxBytes";
    private static final String DOWN_RX_PACKETS = "downRxPackets";
    private static final String DOWN_TX_BYTES = "downTxBytes";
    private static final String DOWN_TX_PACKETS = "downTxPackets";
    private static final String UP_CONTROL_PACKETS = "upControlPackets";
    private static final String TIMESTAMP = "timestamp";
    private final Logger log = LoggerFactory.getLogger(getClass());
    Map<Attachment, Map<BngCounterType, PiCounterCellData>> hostStats;
    private static final String DEVICE_ID = "deviceId";
    private static final String PORT_NUMBER = "portNumber";
    private static final String MAC_ADDRESS = "macAddress";
    private static final String IP_ADDRESS = "ipAddress";
    private static final String SERIAL_NUMBER = "serialNumber";
    private static final String SESSION_ID = "sessionId";

    public BngStatsEventInfo(Map<Attachment, Map<BngCounterType, PiCounterCellData>> hostStats) {
        this.hostStats = hostStats;
    }

    public JsonNode serializeBngEvent(HostService hostService) {
        // Serialize stats in a JSON node
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode bnguStats = mapper.createObjectNode();
        ArrayNode subscribers = mapper.createArrayNode();

        for (Attachment s : this.hostStats.keySet()) {
            // Retrieve the host associated to the specified attachment
            if (hostService.getHostsByMac(s.macAddress()).isEmpty()) {
                log.debug("No host registered with this MAC: {}", s.macAddress().toString());
                continue;
            }
            Host host = hostService.getHostsByMac(s.macAddress()).iterator().next();

            ObjectNode sub = mapper.createObjectNode();
            sub.put(MAC_ADDRESS, s.macAddress().toString());
            sub.put(IP_ADDRESS, s.ipAddress().toString());
            sub.put(SESSION_ID, s.pppoeSessionId());
            sub.put(SUBSCRIBER_S_TAG, s.sTag().toString());
            sub.put(SUBSCRIBER_C_TAG, s.cTag().toString());

            sub.put(SERIAL_NUMBER, host.annotations().value(BngService.ONU_ANNOTATION));
            ConnectPoint oltConnectPoint = getOltConnectPointFromAnnotation(host);
            sub.put(DEVICE_ID, oltConnectPoint.deviceId().toString());
            sub.put(PORT_NUMBER, oltConnectPoint.port().toString());

            sub.put(UP_TERM_BYTES, hostStats.get(s).get(BngCounterType.UPSTREAM_TX).bytes());
            sub.put(UP_TERM_PACKETS, hostStats.get(s).get(BngCounterType.UPSTREAM_TX).packets());

            sub.put(UP_DROP_BYTES, hostStats.get(s).get(BngCounterType.UPSTREAM_DROPPED).bytes());
            sub.put(UP_DROP_PACKETS, hostStats.get(s).get(BngCounterType.UPSTREAM_DROPPED).packets());

            sub.put(DOWN_RX_BYTES, hostStats.get(s).get(BngCounterType.DOWNSTREAM_RX).bytes());
            sub.put(DOWN_RX_PACKETS, hostStats.get(s).get(BngCounterType.DOWNSTREAM_RX).packets());

            sub.put(DOWN_TX_BYTES, hostStats.get(s).get(BngCounterType.DOWNSTREAM_TX).bytes());
            sub.put(DOWN_TX_PACKETS, hostStats.get(s).get(BngCounterType.DOWNSTREAM_TX).packets());

            sub.put(UP_CONTROL_PACKETS, hostStats.get(s).get(BngCounterType.CONTROL_PLANE).packets());

            subscribers.add(sub);
        }
        bnguStats.put(SUBSCRIBERS, subscribers);
        bnguStats.put(TIMESTAMP, Instant.now().toString());
        return bnguStats;
    }

    private ConnectPoint getOltConnectPointFromAnnotation(Host host) {
        // FIXME: when SR patch is merged, take always the host location
        if (host.annotations().keys().contains(BngService.AUX_LOCATIONS_ANNOTATION)) {
            String auxLocationsStr = host.annotations().value(BngService.AUX_LOCATIONS_ANNOTATION)
                    .replaceAll("\\s+", "");
            try {
                return Set.of(auxLocationsStr.split(",")).stream()
                        .map(ConnectPoint::fromString)
                        .map(cp -> new HostLocation(cp, 0L))
                        .collect(Collectors.toSet()).iterator().next();
            } catch (IllegalArgumentException e) {
                return host.location();
            }
        }
        return host.location();
    }

}
