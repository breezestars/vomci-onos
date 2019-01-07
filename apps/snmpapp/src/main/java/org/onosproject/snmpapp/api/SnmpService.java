package org.onosproject.snmpapp.api;

import org.onosproject.net.DeviceId;
import org.snmp4j.smi.OID;
import org.snmp4j.Snmp;
import org.snmp4j.util.TreeEvent;

import java.util.List;
import java.io.IOException;

public interface SnmpService {
    public List<TreeEvent> walk(DeviceId did, OID oid) throws IOException;

    public String get(DeviceId did, OID oid) throws IOException;
}