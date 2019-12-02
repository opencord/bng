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

package org.opencord.bng.config;

import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.config.Config;

/**
 * Configuration for the PPPoE control packet relay service.
 */
public class PppoeRelayConfig extends Config<ApplicationId> {
    public static final String KEY = "pppoerelay";

    private static final String PPPOE_SERVER_CONNECT_POINT = "pppoeServerConnectPoint";
    private static final String OLT_CONNECT_POINT = "oltConnectPoint";
    private static final String PPPOE_MAC_ADDRESS = "pppoeMacAddress";

    @Override
    public boolean isValid() {
        return hasOnlyFields(PPPOE_SERVER_CONNECT_POINT, OLT_CONNECT_POINT, PPPOE_MAC_ADDRESS) &&
                hasFields(PPPOE_SERVER_CONNECT_POINT, OLT_CONNECT_POINT, PPPOE_MAC_ADDRESS);
    }

    /**
     * Gets the PPPoE server connect point.
     *
     * @return PPPoE server connect point
     */
    public ConnectPoint getPppoeServerConnectPoint() {
        return ConnectPoint.deviceConnectPoint(object.path(PPPOE_SERVER_CONNECT_POINT).asText());
    }

    /**
     * Gets the connect point where the OLT is connected to the ASG.
     *
     * @return ASG to OLT connect point
     */
    public ConnectPoint getAsgToOltConnectPoint() {
        return ConnectPoint.deviceConnectPoint(object.path(OLT_CONNECT_POINT).asText());
    }

    /**
     * Gets the PPPoE server MAC address.
     *
     * @return PPPoE server MAC address
     */
    public MacAddress getPppoeMacAddress() {
        return MacAddress.valueOf(object.path(PPPOE_MAC_ADDRESS).asText());
    }
}

