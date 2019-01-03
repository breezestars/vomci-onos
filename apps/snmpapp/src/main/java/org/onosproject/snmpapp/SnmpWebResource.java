package org.onosproject.snmpapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.DeviceId;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.snmpapp.api.SnmpService;
import org.onosproject.snmp.SnmpController;
import org.onosproject.snmp.SnmpDevice;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.smi.OID;
import org.slf4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
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
     * Get MIB by OID.
     *
     * @param DeviceID Device ID for query.
     * @param OID Object ID for query.
     * @return 200 OK
     */
    @GET
    @Path("{DeviceID}/{OID}")
    public Response get(@PathParam("DeviceID") String deviceId, @PathParam("OID") String oid) {
        ObjectNode node = mapper().createObjectNode();
        try {
            DeviceId did = DeviceId.deviceId(deviceId);
            SnmpDevice device = controller.getDevice(did);
            node.put("host", device.getSnmpHost());
            List<TreeEvent> mibs = snmpService.get(did, new OID(oid));
            log.info(mibs.get(0).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok(node).build();
    }
}
