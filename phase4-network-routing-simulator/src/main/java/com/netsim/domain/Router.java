package com.netsim.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a network node (FR-1). Holds its id, computed routing table,
 * interface list, and up/down status for fault injection.
 */
public class Router {

    private final String id;
    private final String label;
    private volatile RoutingTable routingTable;
    private volatile RouterStatus status;
    private final Map<String, Interface> interfaces = new ConcurrentHashMap<>();

    public Router(String id, String label) {
        this.id = id;
        this.label = label;
        this.status = RouterStatus.UP;
        this.routingTable = RoutingTable.empty(id);
    }

    public List<String> neighborIds() {
        return interfaces.values().stream().map(Interface::name).toList();
    }

    public void addInterface(Interface iface) {
        interfaces.put(iface.linkId(), iface);
    }

    public void removeInterface(String linkId) {
        interfaces.remove(linkId);
    }

    public void applyRoutingTable(RoutingTable table) {
        this.routingTable = table;
    }

    public boolean isReachable() {
        return status == RouterStatus.UP;
    }

    public void setStatus(RouterStatus status) {
        this.status = status;
    }

    public RouterStatus getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public Map<String, Interface> getInterfaces() {
        return interfaces;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Router r && r.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Router{" + id + ", status=" + status + '}';
    }
}
