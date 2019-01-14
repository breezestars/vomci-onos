/*
 * Copyright 2016-present Open Networking Foundation
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
package org.onosproject.snmpapp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.snmpapp.api.SnmpService;
import org.onosproject.snmp.SnmpController;
import org.onosproject.snmp.SnmpDevice;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.slf4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Snmp web resrouce.
 */
@Path("v1")
public class SnmpWebResource extends AbstractWebResource {

    private final Logger log = getLogger(getClass());

    private DeviceService deviceService = get(DeviceService.class);

    private SnmpController controller = get(SnmpController.class);

    private SnmpService snmpService = get(SnmpService.class);
    /**
     * SNMP Walk with OID.
     *
     * @param deviceId Device ID for query.
     * @param oid Object ID for query.
     * @return 200 OK
     */
    @GET
    @Path("/walk/{DeviceID}/{OID}")
    public Response walk(@PathParam("DeviceID") String deviceId, @PathParam("OID") String oid) {
        ObjectNode node = mapper().createObjectNode();
        try {
            DeviceId did = DeviceId.deviceId(deviceId);
            SnmpDevice device = controller.getDevice(did);
            node.put("host", device.getSnmpHost());
            List<TreeEvent> mibs = snmpService.walk(did, new OID(oid));
            //log.info("size "+Integer.toString(mibs.size()));
            TreeEvent te = mibs.get(0);
            for (VariableBinding vb : te.getVariableBindings()) {
                node.put(vb.getOid().toString(), vb.getVariable().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok(node).build();
    }
    /**
     * SNMP Get with OID.
     * @param deviceId Device ID for query.
     * @param oid Object ID for query.
     * @return 200 OK
     */
    @GET
    @Path("/get/{DeviceID}/{OID}")
    public Response get(@PathParam("DeviceID") String deviceId, @PathParam("OID") String oid) {
        ObjectNode node = mapper().createObjectNode();
        try {
            DeviceId did = DeviceId.deviceId(deviceId);
            String res = snmpService.get(did, new OID(oid));
            node.put(oid, res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok(node).build();
    }

    /**
     * SNMP Set with OID.
     * @param deviceId Device ID for query.
     * @param oid Object ID for query.
     * @param val The value want to set.
     * @return 200 OK
     */
    @GET
    @Path("/set/{DeviceID}/{OID}/{Value}")
    public Response set(@PathParam("DeviceID") String deviceId, @PathParam("OID") String oid,
            @PathParam("Value") String val) {
        ObjectNode node = mapper().createObjectNode();
        try {
            DeviceId did = DeviceId.deviceId(deviceId);
            String res = snmpService.set(did, new OID(oid), val);
            node.put(oid, res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok(node).build();
    }
}
