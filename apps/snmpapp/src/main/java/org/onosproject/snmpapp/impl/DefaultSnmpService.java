/*
 * Copyright 2018-present Open Networking Foundation
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
package org.onosproject.snmpapp.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.snmpapp.api.SnmpService;
import org.onosproject.net.DeviceId;
import org.onosproject.snmp.SnmpController;
import org.onosproject.snmp.SnmpDevice;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.CommunityTarget;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeUtils;
import org.snmp4j.util.TreeEvent;
import org.slf4j.Logger;

import java.util.List;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;
/**
 * SNMP Service.
 */
@Component(immediate = true)
@Service
public class DefaultSnmpService implements SnmpService {
    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected SnmpController snmpController;

    private static final int MAX_SIZE_RESPONSE_PDU = 65535;
    private static final int MAX_REPETITIONS = 50;      // Only 42 directed ports on our devices

    @Activate
    public void activate() {
        log.info("Snmp Service Started!");
    }

    @Deactivate
    public void deactivate() {
        log.info("Snmp Service Stoped!");
    }

    @Override
    public List<TreeEvent> walk(DeviceId did, OID oid) throws IOException {
        SnmpDevice device = snmpController.getDevice(did);
        String deviceHost = device.getSnmpHost();
        String devicePort = Integer.toString(device.getSnmpPort());
        TransportMapping transport = new DefaultUdpTransportMapping();
        transport.listen();
        Snmp snmp = new Snmp(transport);
        CommunityTarget target = setTarget(deviceHost, devicePort, "v2c");
        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        treeUtils.setMaxRepetitions(MAX_REPETITIONS);
        // TODO add more notification and transport close for good.
        return treeUtils.getSubtree(target, oid);
    }

    @Override
    public String set(DeviceId did, OID oid, String val) throws IOException {
        SnmpDevice device = snmpController.getDevice(did);
        String deviceHost = device.getSnmpHost();
        String devicePort = Integer.toString(device.getSnmpPort());

        PDU pdu = new PDU();
        pdu.setType(PDU.SET);
        VariableBinding vb;
        Variable v = new OctetString(val);
        vb = new VariableBinding(oid, v);
        pdu.add(vb);
        TransportMapping transport = new DefaultUdpTransportMapping();
        transport.listen();
        Snmp snmp = new Snmp(transport);
        CommunityTarget target = setTarget(deviceHost, devicePort, "v2c");
        ResponseEvent response = snmp.send(pdu, target);
        transport.close();
        if (response == null) {
            return "set failed";
        } else {
            PDU res = response.getResponse();
            if (pdu.getErrorStatus() == PDU.noError) {
                return val;
            } else {
                return "set unsuccessful";
            }
        }
    }

    @Override
    public String get(DeviceId did, OID oid) throws IOException {
        SnmpDevice device = snmpController.getDevice(did);
        String deviceHost = device.getSnmpHost();
        String devicePort = Integer.toString(device.getSnmpPort());

        PDU pdu = new PDU();
        pdu.setType(PDU.GET);
        pdu.add(new VariableBinding(oid));
        TransportMapping transport = new DefaultUdpTransportMapping();
        transport.listen();
        Snmp snmp = new Snmp(transport);
        CommunityTarget target = setTarget(deviceHost, devicePort, "v2c");
        ResponseEvent response = snmp.send(pdu, target);
        transport.close();
        PDU res = response.getResponse();
        if (res == null) {
            return "Get Nothing";
        } else {
            // get multiple response by adding VB to the sender PDU.
            VariableBinding vb = res.get(0);
            return vb.getVariable().toString();
        }
    }

    private CommunityTarget setTarget(String host, String port, String snmpVersion) {
        return setTarget(host, port, snmpVersion, "public");
    }

    private CommunityTarget setTarget(String host, String port, String snmpVersion, String community) {
        CommunityTarget target;
        Address targetAddress = GenericAddress.parse("udp:" + host + "/" + port);

        target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(targetAddress);
        target.setRetries(3);
        target.setTimeout(1000L * 3L);
        switch (snmpVersion) {
            case "v1":
                target.setVersion(SnmpConstants.version1);
                break;
            case "v3":
                target.setVersion(SnmpConstants.version3);
                break;
            default:
                target.setVersion(SnmpConstants.version2c);
        }
        target.setMaxSizeRequestPDU(MAX_SIZE_RESPONSE_PDU);
        return target;
    }
}